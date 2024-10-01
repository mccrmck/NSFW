NS_Poem4OJKOS : NS_SynthModule {
    classvar <isSource = true;
    var buffer, bufferPath;
    var ampBus;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_poem4ojkos,{
                var numChans = NSFW.numChans;
                var bufnum   = \bufnum.kr;
                var frames   = BufFrames.kr(bufnum);
                var trig     = \trig.tr(0);
                var sig, pos = Phasor.ar(TDelay.ar(T2A.ar(trig),0.04),BufRateScale.kr(bufnum) * \rate.kr(1),\offset.kr(0) * frames,frames);
                pos = SelectX.ar(DelayN.kr(\which.kr(0),0.04),[pos, pos * LFDNoise1.kr(1).range(0.9,1.1)]);
                sig = BufRd.ar(2,bufnum,pos % frames,4);
                sig = sig * Env([1,0,1],[0.04,0.04]).ar(0,trig + Changed.kr(\which.kr));

                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));

                NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )  
            }).add
        }
    }

    init {
        this.initModuleArrays(6);
        this.makeWindow("Poem4OJKOS", Rect(0,0,240,180));

        bufferPath = "audio/poem.wav".resolveRelative;

        fork{
            buffer = Buffer.read(modGroup.server, bufferPath);
            modGroup.server.sync;
            synths.add( Synth(\ns_poem4ojkos,[\bus,bus,\bufnum,buffer],modGroup) )
        };

        controls.add(
            NS_Fader("rate",ControlSpec(0.25,1,\exp),{ |f| synths[0].set(\rate,f.value) },'horz',initVal:1)
        );
        assignButtons[0] = NS_AssignButton(this, 0, \fader).maxWidth_(45);

        controls.add(
            NS_Switch(["dry","wet"],{ |switch| synths[0].set(\which,switch.value) },'horz')
        );
        assignButtons[1] = NS_AssignButton(this, 1, \switch).maxWidth_(45);

        controls.add(
            Button()
            .states_([["trig",Color.black, Color.white],["trig",Color.white,Color.black]])
            .action_({ |but|
                if(but.value == 1,{
                    synths[0].set(\trig,but.value)
                })
            })
        );
        assignButtons[2] = NS_AssignButton(this, 2, \button).maxWidth_(45);

        controls.add(
            NS_Fader("offset",ControlSpec(0,1,\lin),{ |f| synths[0].set(\trig,1,\offset,f.value) },'horz',initVal:0)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.add(
            NS_Fader("amp",\amp,{ |f| synths[0].set(\amp,f.value) },'horz',initVal:1)
        );
        assignButtons[4] = NS_AssignButton(this, 4, \fader).maxWidth_(45);

        controls.add(
            Button()
            .states_([["▶",Color.black,Color.white],["stop",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                strip.inSynthGate_(val);
                synths[0].set(\trig,val,\thru, val)
            })
        );
        assignButtons[5] = NS_AssignButton(this, 5, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                HLayout( controls[0], assignButtons[0] ),
                HLayout( controls[1], assignButtons[1] ),
                HLayout( controls[2], assignButtons[2] ),
                HLayout( controls[3], assignButtons[3] ),
                HLayout( controls[4], assignButtons[4] ),
                HLayout( controls[5], assignButtons[5] ),
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    freeExtra {
        buffer.free
    }

    *oscFragment {       
        ^OSC_Panel(horizontal: false, widgetArray:[
            OSC_Fader(horizontal: true),
            OSC_Switch(mode: 'slide',numPads: 2),
            OSC_Fader(horizontal: true, snap: true),
            OSC_Button(mode:'push'),
            OSC_Panel(widgetArray: [
                OSC_Fader(horizontal: true),
                OSC_Button(width:"20%")
            ])      
        ],randCol:true).oscString("Poem4OJKOS")
    }
}
