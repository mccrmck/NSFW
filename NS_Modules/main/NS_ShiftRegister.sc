NS_ShiftRegister : NS_SynthModule {
    classvar <isSource = true;

    // pretty sure I got this synthDef from Alejandro Olarte, but I can't remember when
    *initClass {
        ServerBoot.add{
            SynthDef(\ns_shiftRegister,{
                var numChans = NSFW.numChans;
                var freq = \freq.kr(4);
                var sr = SampleRate.ir * \sRate.kr(1);
                var bits = \bits.ar(32);  
                var bitsRaised = 2 ** bits;
                var t = Phasor.ar(DC.ar(0), freq * (bitsRaised / sr), 0, bitsRaised - 1 );
                var array = [
                    t * (( (t>>64) | (t>>8) ) & (63 & (t>>4)) ),
                    t * (( (t>>9)  | (t>>13)) & (25 & (t>>6)) ),
                    t * (( (t>>5)  | (t>>8) ) & 63),
                    t * (((t>>11)  & (t>>8) ) & (123 & (t>>3)) ),
                    t * (t>>8 * ((t>>15) | (t>>8)) & (20 | (t>>19) * 5>>t | (t>>3))),
                    t * (t>>( (t>>9) | (t>>8) ) & (63 & (t>>4)) ),
                    (t>>7 | t | t>>6) * 10 + 4 * (t & t>>13 | t>>6 )
                ];

                var sig = SelectX.ar(\which.kr(0).clip(0,array.size - 1).lag(0.1),array);

                sig = sig % bitsRaised;
                sig = sig * (0.5 ** (bits-1) ) - 1;
                sig = LeakDC.ar(sig) * -18.dbamp;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
                NS_Out(sig,numChans,\bus.kr,\mix.kr(1),\thru.kr(0))
            }).add
        }
    }

    init {
        this.initModuleArrays(5);
        this.makeWindow("ShiftRegister", Rect(0,0,300,240));

        synths.add( Synth(\ns_shiftRegister,[\bus,bus],modGroup) );

        controls.add(
            NS_XY("sRate",ControlSpec(0.01,1,\exp),"bits",ControlSpec(8,32,\exp),{ |xy| 
                synths[0].set(\sRate,xy.x, \bits, xy.y);
            },[1,32]).round_([0.01,0.1])
        );
        assignButtons[0] = NS_AssignButton(this, 0, \xy);

        controls.add(
           NS_Fader("freq",ControlSpec(0.01,250,\exp),{ |f| synths[0].set(\freq, f.value) },initVal: 4).maxWidth_(45) 
        );
        assignButtons[1] = NS_AssignButton(this, 1, \fader).maxWidth_(45);

        controls.add(
           NS_Switch((0..6),{ |switch| synths[0].set(\which,switch.value) },'horz')
        );
        assignButtons[2] = NS_AssignButton(this, 2, \switch).maxWidth_(45);
         
        controls.add(
            NS_Fader("mix",ControlSpec(0,1,\lin),{ |f| synths[0].set(\mix, f.value) },initVal: 1).maxWidth_(45)
        );
        assignButtons[3] = NS_AssignButton(this, 3, \fader).maxWidth_(45);

        controls.add(
            Button()
            .maxWidth_(45)
            .states_([["▶",Color.black,Color.white],["bypass",Color.white,Color.black]])
            .action_({ |but|
                var val = but.value;
                synths[0].set(\thru,val);
                strip.inSynthGate_(val);
            })
        );
        assignButtons[4] = NS_AssignButton(this, 4, \button).maxWidth_(45);

        win.layout_(
            VLayout(
                HLayout(
                    VLayout( controls[0], assignButtons[0],),
                    VLayout( controls[1], assignButtons[1] ),
                    VLayout( controls[3], assignButtons[3], controls[4], assignButtons[4]  )
                ),
                HLayout( controls[2], assignButtons[2] )
            )
        );

        win.layout.spacing_(4).margins_(4)
    }

    *oscFragment {       
        ^OSC_Panel(widgetArray:[
            OSC_XY(snap:true),
            OSC_Fader("15%",snap:true),
            OSC_Switch("15%",columns: 1, mode: 'slide',numPads: 7),
            OSC_Panel("15%",horizontal: false, widgetArray: [
                OSC_Fader(),
                OSC_Button(height:"20%")
            ])
        ],randCol:true).oscString("ShiftRegister")
    }
}
