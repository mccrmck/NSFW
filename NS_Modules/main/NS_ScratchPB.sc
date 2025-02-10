NS_ScratchPB : NS_SynthModule {
    classvar <isSource = false;
    var buffer;
    var freqBus, mulBus, modFreqBus, modMulBus, mixBus;

    *initClass {
        ServerBoot.add{

            SynthDef(\ns_scratchPBRec,{
                var numChans = NSFW.numChans;
                var sig      = In.ar(\bus.kr,numChans);
                var bufnum   = \bufnum.kr;
                var pos      = Phasor.ar(DC.ar(0),\rec.kr(1),0,BufFrames.kr(bufnum));
                var rec      = BufWr.ar(sig,bufnum,pos);
                NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
            }).add;

            SynthDef(\ns_scratchPB,{
                var numChans = NSFW.numChans;
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum) - 1;
                var modMul   = \modMul.kr(1);
                var freq     = \freq.kr(4) * LFDNoise1.kr(\modFreq.kr(1)).linexp(-1,1,modMul.reciprocal,modMul);
                var scratch  = LFDNoise0.ar(freq,\mul.kr(0.5));
                var pos      = Phasor.ar(DC.ar(0),BufRateScale.kr(bufnum) * (scratch + 1) * scratch.sign,0,frames);

                var sig = BufRd.ar(numChans,bufnum,pos);
                sig = HPF.ar(sig,20).tanh;

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )
            }).add;
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("ScratchPB", Rect(0,0,240,150));
        synths = Array.newClear(2);

        freqBus    = Bus.control(modGroup.server,1).set(4);
        mulBus     = Bus.control(modGroup.server,1).set(0.5);
        modFreqBus = Bus.control(modGroup.server,1).set(1);
        modMulBus  = Bus.control(modGroup.server,1).set(1);
        mixBus     = Bus.control(modGroup.server,1).set(1);

        buffer = Buffer.alloc(modGroup.server, modGroup.server.sampleRate * 2, NSFW.numChans);
        synths.put(0, Synth(\ns_scratchPBRec,[\bufnum,buffer,\bus,bus],modGroup) );

        controls[0] = NS_Control(\freq,ControlSpec(0.1,36,1.5),4)
        .addAction(\synth,{ |c| freqBus.set( c.value ) });
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(30);

        controls[1] = NS_Control(\mul,ControlSpec(0.01,1,\lin),0.5)
        .addAction(\synth,{ |c| mulBus.set( c.value ) });
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(30);

        controls[2] = NS_Control(\modFreq,ControlSpec(0.1,10,\exp),1)
        .addAction(\synth,{ |c| modFreqBus.set( c.value ) });
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(30);

        controls[3] = NS_Control(\modMul,ControlSpec(1,4,\lin),1)
        .addAction(\synth,{ |c| modMulBus.set( c.value ) });
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(30);

        controls[4] = NS_Control(\mix,ControlSpec(0,1,\lin),1)
        .addAction(\synth,{ |c| mixBus.set( c.value ) });
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(30);

        controls[5] = NS_Control(\bypass, ControlSpec(0,1,\lin,1), 0)
        .addAction(\synth,{ |c| 
            var val = c.value;
            strip.inSynthGate_(val);
            synths[0].set(\rec, 1 - val);

            if( val == 0,{
                synths[1].set(\gate,0);
                synths[1] = nil
            },{
                synths.put(1,
                    Synth(\ns_scratchPB,[
                        \bufnum,buffer,
                        \freq,freqBus.asMap,
                        \mul, mulBus.asMap,
                        \modFreq,modFreqBus.asMap,
                        \modMul,modMulBus.asMap,
                        \mix, mixBus.asMap,
                        \bus,bus
                    ],modGroup,\addToTail)
                )
            });
        });
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(30);

        win.layout_(
            VLayout(
                HLayout( NS_ControlFader(controls[0])                , assignButtons[0] ),
                HLayout( NS_ControlFader(controls[1])                , assignButtons[1] ),
                HLayout( NS_ControlFader(controls[2])                , assignButtons[2] ),
                HLayout( NS_ControlFader(controls[3])                , assignButtons[3] ),
                HLayout( NS_ControlFader(controls[4])                , assignButtons[4] ),
                HLayout( NS_ControlButton(controls[5],["▶","bypass"]), assignButtons[5] ), 
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        buffer.free;
        freqBus.free; 
        mulBus.free;
        modFreqBus.free; 
        modMulBus.free;
        mixBus.free;
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_XY(snap:true),
            OSC_Panel(width: "20%",horizontal: false, widgetArray: [
                OSC_Fader(),
                OSC_Button(height:"20%")
            ])
        ],randCol:true).oscString("ScratchPB")
    }
}
