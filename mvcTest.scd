(
~spec1 = \freq.asSpec;
~spec2 = \db.asSpec;
~model = (freq: 440, amp: -9);
~controller = SimpleController( ~model );
~controller.put(\freq,{ |theChanger, what ...moreArgs| // ~model, \key, otherArgs
    ~view1.value_( ~spec1.unmap(theChanger[\freq]) )
});
~controller.put(\amp,{ |theChanger, what ...moreArgs| 
    ~view2.value_( ~spec2.unmap(theChanger[\amp]) ) 
});

~win = Window("test").front;
~view1 = Slider(~win,Rect(0,0,200,200)).action_({ |sl|
    sl.value.postln;
    ~model[\freq] = ~spec1.map(sl.value)
});
~view2 = NumberBox(~win,Rect(0,200,200,200)).action_({ |nb|
    ~model[\amp] = ~spec2.map(nb.value)
});

~win.onClose_{ ~controller.remove }

);

~model[\freq] = 234; ~model.changed(\freq);
~model[\amp] = -14.5; ~model.changed(\amp);
~model

///


~controls.asPairs
~controls['amp'] = 0.1; ~controls.changed(\value)

(
/////////////////////////////////////////////////////////////////
// DESCRIBE THE MODEL
~controls = (
	amp: 0.8,
	cutoff: 700,
	res: 0.7
);

/////////////////////////////////////////////////////////////////
// DESCRIBE THE UI
// Note that we're not connecting to anything, only creating our controls and layout.
~views = ();
~views.use {
	~parent = View().layout_(
		VLayout(
			~ampView =     NumberBox().clipLo_(0).clipHi_(1).fixedWidth_(80),
			~cutoffView =  NumberBox().clipLo_(80).clipHi_(10000).fixedWidth_(80),
			~resView =     NumberBox().clipLo_(0.1).clipHi_(1.0).fixedWidth_(80),
		)
	).front;
};

/////////////////////////////////////////////////////////////////
// MAKE A SYNTH
~synth = {
    var freq, trig, sig;
    trig = Impulse.ar(8);
    freq = Demand.ar(
trig,
        0,
        32 + Dseq([0, 2, 3, 6, 5, 7, 3], inf) + Drand([0, 0, 0, 0, 12], inf)
    );

    sig = LFSaw.ar(freq * [0, 0.01, -0.0342, 0.042].midiratio).sum.clip2(1);
    sig = BLowPass.ar(sig, \cutoff.ar(100), \res.ar(1.0));

    sig = sig * Env.perc(0.01, TExpRand.ar(0.1, 0.8, trig)).ar(gate:trig);
    sig = sig * \amp.ar(1.0);
}.play(args: ~controls.asPairs);

/////////////////////////////////////////////////////////////////
// CONNECT THE UI
~updateViews = {
	~views[\ampView].value      = ~controls[\amp];
	~views[\cutoffView].value   = ~controls[\cutoff];
	~views[\resView].value      = ~controls[\res];
};

~updateControls = {
	~controls[\amp] 			= ~views[\ampView].value;
	~controls[\cutoff] 			= ~views[\cutoffView].value;
	~controls[\res] 			= ~views[\resView].value;

	~controls.changed(\value);
};

~updateSynth = {
	~synth.set(*~controls.asPairs);
};

~controls.addDependant(~updateViews);
~controls.addDependant(~updateSynth);

~views[\parent].onClose({ ~views[\parent].removeDependant(~controls) });
~views[\ampView].action = ~updateControls;
~views[\cutoffView].action = ~updateControls;
~views[\resView].action = ~updateControls;

// Now that everything is connected, fire this to update.
~controls.changed(\value);
)


//

NS_SynthModule {

    *new {
        ^super.new.modGroup_(group).bus_(bus).strip_(strip).slotIndex_(slotIndex).init.makeView
    }
}

NS_Synth : NS_SynthModule {

    init {
        var server   = modGroup.server;
        var nsServer = NSFW.servers[server.name];
        var numChans = strip.numChans;

        controls = (
            freq: 440,
            rq: 0.1
        );

        nsServer.addDefCreateSynth(
            modGroup,
            ("ns_synth" ++ numChans).asSymbol,
            { /*ugenGraph*/ },
            controls.asPairs,
            { |synth| synths.add(synth) }
        );


        SimpleController(controls)
        .put(\freq,{ |obj, what, value|

            synths.set(\freq, value)
             
        })

        // alternatively
        controls.addDependant


    }

    makeView {

        View().layout_(

            NS_Fader(\freq, ControlSpec)

        )

    }
}

(
~controls = ( 
    freq: (
        spec: \freq.asSpec,
        value: 440,
        //actions: (),
    ), 
    string: (
        spec: nil,
        value: "string"
        //actions: (),
    ),
    routing: ()
)
)

~controller = SimpleController(~controls)
~controller.put(\freq,{ |model, key| [model,key].postln })
~controller.put(\freq,{ |model, key| 2.postln; })
~controller.put(\value,{ |model, key| key.postln })
~controls['freq']['value'] = 50; ~controls.changed(\freq)
~controller.update(~controls, \freq,123)
Object.dependantsDictionary.dopostln


f = { |model, key, args|

    switch(key)
    { \freq }{ model['freq']['actions'].do(_.value(model)) }
    { \string }{ model['string']['actions'].do(_.value(model)) }

}
e.addDependant(f)
e.dependants
e.releaseDependants
e.removeDependant(f)

~controls['freq']['value'] = 20.rrand(20000); e.changed(\freq)

(
w = Window().layout_(
    HLayout(
        v = TextField().action_({ |st| ~controls['string']['value'] = st.value; ~controls.changed(\string) }),
        y = Slider().value_(~controls['freq']['value'].map())
        .action_({ |sl| 
            ~controls['freq']['value'] = ~controls['freq']['value'].map(sl.value);
            ~controls.changed(\freq)
        })
    )
).front;
~controller.put(\qt,{ |ctrls| v.value_(ctrls['freq']['value']) });
~controller.put(\qt,{ |ctrls| y.value_(ctrls['string']['value']) });
w.onClose({ })
)

e.postln
d



a = NS_Control(\testString, \string, "thisString")
a.value
a.spec = nil
\string.asSpec

nil.constrain(5)
ControlSpec

/* from Object source
changed { arg what ... moreArgs;
    dependantsDictionary.at(this).copy.do({ arg item;
        item.update(this, what, *moreArgs);
    });
}
update { arg theChanged, theChanger }	// respond to a change in a model

// from Function source
update { |obj, what ... args| this.value(obj, what, *args) }
*/


Event.update
