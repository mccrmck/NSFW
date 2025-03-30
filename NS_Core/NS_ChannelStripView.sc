NS_ChannelStrip1 : NS_ControlModule {
    var <group, <numSlots;
    var stripGroup, <inGroup, allSlots, <slotGroups, <faderGroup;
    var <slots;
    var <stripBus;
    var <inSynth, fader;


    var <>paused = false;

    *initClass {
        ServerBoot.add{
            SynthDef(\ns_stripIn,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
                sig = sig * \thru.kr(0);
                ReplaceOut.ar(\outBus.kr,sig);
            }).add;

            SynthDef(\ns_stripFader,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\bus.kr, numChans);
                var mute = 1 - \mute.kr(0,0.01); 
                sig = ReplaceBadValues.ar(sig);
                sig = sig * mute;
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(0,0.01));

                ReplaceOut.ar(\bus.kr, sig)
            }).add;

            SynthDef(\ns_stripSend,{
                var numChans = NSFW.numChans;
                var sig = In.ar(\inBus.kr,numChans);
                sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1,0.01));
                Out.ar(\outBus.kr,sig);
            }).add
        }
    }


    *new { |inGroup, numModules = 6|
        ^super.new.init(inGroup, numModules)
    }

    init { |inGroup, numModules|
        group    = inGroup;
        numSlots = numModules;
        this.initControlArrays(3);

        stripBus   = Bus.audio(group.server, NSFW.numChans);

        slots      = numSlots.collect({ NS_ModuleSlot() });

        stripGroup = Group(group,\addToTail);
        inGroup    = Group(stripGroup,\addToTail);
        allSlots   = Group(stripGroup,\addToTail);
        slotGroups = numSlots.collect({ |i| Group(allSlots,\addToTail) });
        faderGroup = Group(stripGroup,\addToTail);

        fader = Synth(\ns_stripFader,[\bus,stripBus],faderGroup);
        
        controls[0] = NS_Control(\amp,\db)
        .addAction(\synth,{ |c| /* fader.set(\amp, c.value.dbamp) */ });
        assignButtons[0] = NS_AssignButton(this,0,\fader).maxWidth_(30);


        controls[1] = NS_Control(\visible,ControlSpec(0,0,'lin',1))
        .addAction(\synth,{ |c| /* show/hide module windows */ });
        assignButtons[1] = NS_AssignButton(this,1,\button).maxWidth_(30);


        controls[2] = NS_Control(\mute,ControlSpec(0,1,'lin',1))
        .addAction(\synth,{ |c| /* fader.set(\mute, c.value) */ });
        assignButtons[2] = NS_AssignButton(this,2,\button).maxWidth_(30);
    }

    inSynth_ { |synthKey|
        inSynth = Synth(synthKey.asSymbol,[
            \inBus,stripBus, \outBus, stripBus
        ],inGroup)
    }

    moduleArray {}

    inSynthGate {} // this needs an overhaul

    free {}

    pause {}
    
    unpause {}

    saveExtra {}

    loadExtra {}
    
}

NS_ChannelStripView {
    var <view;
    var highlight = false;

    *new { |channelStrip|
        ^super.new.init(channelStrip)
    }

    init { |strip|
        var controls = strip.controls;
        var assignButtons = strip.assignButtons;
        var modSinks = strip.slots.collect({ |slot| NS_ModuleSlotView(slot) });

        view = UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = NS_Style.radius;
            var fill = if(highlight,{ NS_Style.highlight },{ NS_Style.transparent });

            Pen.fillColor_(fill);
            Pen.addRoundedRect(Rect(0, 0, w, h), r, r);
            Pen.fill;
        })
        .layout_(
            VLayout(
                VLayout( *modSinks ),
                HLayout(
                    NS_ControlFader(controls[0]).round_(1).showLabel_(false),
                    assignButtons[0]
                ),
                HLayout( 
                    NS_ControlButton(controls[1],[
                        ["S", Color.black, Color.yellow]
                    ]),
                    assignButtons[1],
                    NS_ControlButton(controls[2], [
                        ["M", NS_Style.muteRed, NS_Style.textDark],
                        [NS_Style.play, NS_Style.playGreen, NS_Style.bGroundDark]
                    ]),
                    assignButtons[2]
                )
            )
        );
    }

    toggleAllVisible { }

    highlight { |bool|
        highlight = bool;
        view.refresh
    }

    asView { ^view }
}
