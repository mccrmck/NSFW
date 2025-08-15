NS_GrainFuncs {
    

    // helperFunctions
    *rampToSlope { |phase|
        var history = Delay1.ar(phase);
        var delta = phase - history;
        ^delta.wrap(-0.5, 0.5)
    }

    *rampToTrig { |phase|
        var history = Delay1.ar(phase);
        var delta = phase - history;
        var sum = phase + history;
        var trig = (delta / sum).abs > 0.5;
        ^Trig1.ar(trig, SampleDur.ir)
    }

    *subSampleOffset { |phase, slope, trig|
        var sampleCount = phase - (slope < 0) / slope;
        ^Latch.ar(sampleCount, trig)
    }

    *accumSubSample { |trig, subSampleOffset|
        var hasTriggered = PulseCount.ar(trig) > 0;
        var accum = Duty.ar(SampleDur.ir, trig, Dseries(0, 1)) * hasTriggered;
        ^(accum + subSampleOffset)
    }

    *rampSubSample { |trig, slope, subSampleOffset|
        var hasTriggered = PulseCount.ar(trig) > 0;
        var accum = Sweep.ar(trig, slope * SampleRate.ir) * hasTriggered;
        ^(accum + (slope * subSampleOffset))
    }

    // multiChannelFunctions

    *mChanTrigger { |numChannels, trig|
        ^numChannels.collect({ |chan|
            PulseDivider.ar(trig, numChannels, numChannels - 1 - chan);
        })
    }

    *mChanAccumSubSample { |triggers, subSampleOffsets|
        ^triggers.collect({ |localTrig, i|
            this.accumSubSample(localTrig, subSampleOffsets[i]);
        })
    }

    *mChanRampSubSample { |triggers, slopes, subSampleOffsets|
        ^triggers.collect({ |localTrig, i|
            this.rampSubSample(localTrig, slopes[i], subSampleOffsets[i]);
        })
    }

    *mChanDwhite { |triggers|
        var demand = Dwhite(-1.0, 1.0);
        ^triggers.collect({ |localTrig|
            Demand.ar(localTrig, DC.ar(0), demand)
        })
    }

    *mChanDbrown { |triggers|
        var demand = Dbrown(-1.0, 1.0, 0.01);
        ^triggers.collect({ |localTrig|
            Demand.ar(localTrig, DC.ar(0), demand)
        })
    }

    *mChanDxrand { |triggers, reset, arrayOfItems, numOfItems, repeatItem|
        var demand = Ddup(repeatItem, Dxrand([Dser(arrayOfItems, numOfItems)], inf));
        ^triggers.collect({ |localTrig|
            Demand.ar(localTrig + reset, reset, demand)
        })
    }

    *mChanDseq { |triggers, reset, arrayOfItems, numOfItems, repeatItem|
        var demand = Ddup(repeatItem, Dseq([Dser(arrayOfItems, numOfItems)], inf));
        ^triggers.collect({ |localTrig|
            Demand.ar(localTrig + reset, reset, demand)
        })
    }

    *mChanDseries { |triggers, reset, numOfItems, repeatItem|
        var demand = Ddup(repeatItem, Dseq([Dseries(0, 1, numOfItems)], inf));
        ^triggers.collect({ |localTrig|
            Demand.ar(localTrig + reset, reset, demand)
        })
    }

    *mChanBufRd { |triggers, phases, arrayOfBuffers, numOfBuffers, repeatBuffer, loop|
        var bufferIndex = this.multiChannelDseries(triggers, DC.ar(0), numOfBuffers, repeatBuffer);
        var playbufs = arrayOfBuffers.collect({ |buffer|
            BufRd.ar(1, buffer, phases * BufFrames.kr(buffer), loop: loop, interpolation: 4);
        });
        ^Select.ar(bufferIndex, playbufs)
    }

    // maskingFunctions

    *triggerMask { |trig, maskOn, arrayOfBinaries, numOfBinaries|
        var demand = Dseq([Dser(arrayOfBinaries, numOfBinaries)], inf);
        var triggerMask = Demand.ar(trig, DC.ar(0), demand);
        ^(trig * Select.ar(maskOn, [DC.ar(1), triggerMask]))
    }

    *burstMask { |trig, burst = 16, rest = 0|
        var demand = Dseq([Dser([1], burst), Dser([0], rest)], inf);
        ^(trig * Demand.ar(trig, DC.ar(0), demand))
    }

    *probabilityMask { |trig, prob = 1|
        ^(trig * CoinGate.ar(prob, trig))
    }

    *channelMask { |trig, channelMask, centerMask, numSpeakers = 2| // nunChannels?
        var arrayOfPositions = Array.series(numSpeakers, -1 / numSpeakers, 2 / numSpeakers).wrap(-1.0, 1.0);
        var channelPos = arrayOfPositions.collect { |pos| Dser([pos], channelMask) };
        ^Demand.ar(trig, DC.ar(0), Dseq(channelPos ++ Dser([0], centerMask), inf))
    }

    // eventsCircular

    // *eventDataCircular = { |rate, reset|

    //     var eventPhase, eventSlope, eventTrigger;

    //     eventPhase = VariableRamp.ar(rate, reset);
    //     eventSlope = self.helperFunctions[\rampToSlope].(eventPhase);

    //     eventPhase = Delay1.ar(eventPhase);
    //     eventTrigger = self.helperFunctions[\rampToTrig].(eventPhase);

    //     ^(
    //         phase: eventPhase,
    //         slope: eventSlope,
    //         trigger: eventTrigger
    //     );
    // }

    // oneShotRampFunctions

    *oneShotSubDivs { |trig, arrayOfSubDivs, numOfSubDivs, duration|
        var hasTriggered = PulseCount.ar(trig) > 0;
        var subDiv = Ddup(2, Dseq(arrayOfSubDivs, numOfSubDivs)) * duration;
        ^Duty.ar(subDiv, trig, subDiv) * hasTriggered
    }

    *oneShotRamp { |trig, duration, cycles = 1|
        var hasTriggered = PulseCount.ar(trig) > 0;
        var phase = Sweep.ar(trig, 1 / duration).clip(0, cycles);
        ^(phase * hasTriggered)
    }

    *oneShotRampToTrig { |phase|
        var compare = phase > 0;
        var delta = HPZ1.ar(compare);
        ^(delta > 0)
    }

    *oneShotBurstToTrig { |phaseScaled|
        var phaseStepped = phaseScaled.ceil;
        var delta = HPZ1.ar(phaseStepped);
        ^(delta > 0)
    }

    // eventsOneShot
    *oneShotEventData { |initTrigger, duration, arrayOfSubDivs, numOfSubDivs|

        var seqOfSubDivs = this.oneShotSubDivs(
            initTrigger, arrayOfSubDivs, numOfSubDivs, duration
        );
        var eventPhaseScaled = this.oneShotRamp(initTrigger, seqOfSubDivs, numOfSubDivs);

        var eventTrigger = this.oneShotBurstToTrig(eventPhaseScaled);

        var eventPhase = eventPhaseScaled.wrap(0, 1);
        var eventSlope = this.rampToSlope(eventPhase);

        ^(
            phase: eventPhase,
            slope: eventSlope,
            trigger: eventTrigger
        )
    }

    *oneShotMeasureData { |initTrigger, duration|

        var measurePhase   = this.oneShotRamp(initTrigger, duration);
        var measureSlope   = this.rampToSlope(measurePhase);
        var measureTrigger = this.oneShotRampToTrig(measurePhase);

        ^(
            phase: measurePhase,
            slope: measureSlope,
            trigger: measureTrigger
        )
    }
}
