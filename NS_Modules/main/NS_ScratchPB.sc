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
                //var sig      = In.ar(\bus.kr,numChans);
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var modMul   = \modMul.kr(1);
                var freq     = \freq.kr(4);// * LFDNoise1.kr(\modFreq.kr(1)).linexp(-1,1,modMul.reciprocal,modMul);
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
        this.initModuleArrays(4);
        this.makeWindow("ScratchPB", Rect(0,0,210,330));
        synths = Array.newClear(2);

        freqBus = Bus.control(modGroup.server,1).set(4);
        mulBus = Bus.control(modGroup.server,1).set(0.5);
        modFreqBus = Bus.control(modGroup.server,1).set(1);
        modMulBus = Bus.control(modGroup.server,1).set(1);
        mixBus = Bus.control(modGroup.server,1).set(1);

        buffer = Buffer.alloc(modGroup.server, modGroup.server.sampleRate * 2, NSFW.numChans);
        synths.put(0, Synth(\ns_scratchPBRec,[\bufnum,buffer,\bus,bus],modGroup) );

        controls.add(
            NS_XY("freq",ControlSpec(0.1,36,1.5),"mul",ControlSpec(0.01,1,\lin),{ |xy|
                freqBus.set(xy.x);
                mulBus.set(xy.y);
            },[4,0.5]).round_([0.1,0.01])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
            NS_XY("modFreq",ControlSpec(0.1,10,\exp),"modMul",ControlSpec(1,4,\lin),{ |xy|
                modFreqBus.set(xy.x);
                modMulBus.set(xy.y);
            },[1,1]).round_([0.1,0.1])
        );
        assignButtons[1] = NS_AssignButton(this, 1, \xy);

        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| mixBus.set(f.value) },initVal:1).maxWidth_(45)
        );
        assignButtons[2] = NS_AssignButton(this, 2, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
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
            })
        );
        assignButtons[3] = NS_AssignButton(this, 3, \button).maxWidth_(45);

        win.layout_(
            HLayout(
                VLayout( controls[0], assignButtons[0], controls[1], assignButtons[1],),
                VLayout( controls[2], assignButtons[2], controls[3], assignButtons[3] )
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
