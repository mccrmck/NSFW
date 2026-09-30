NS_PhasorBus {
    var bus;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |server|
        bus = Bus.audio(server, 3);
    }

    phaseBus { ^bus.subBus(0) }
    trigBus  { ^bus.subBus(1) }
    slopeBus { ^bus.subBus(2) }

    phase { ^this.phaseBus.ar }
    trig  { ^this.trigBus.ar }
    slope { ^this.slopeBus.ar }

    phaseAsMap { ^this.phaseBus.asMap }
    trigAsMap  { ^this.trigBus.asMap }
    slopeAsMap { ^this.slopeBus.asMap }

    free { bus.free }
}

NS_PhasorBuf {
    var buf;

    *new {

    }

    init {
        buf = Buffer

    }

}

NS_AbstractPhasor {
    var <phasorBus;

    phase { ^phasorBus.phase }
    trig  { ^phasorBus.trig }
    slope { ^phasorBus.slope }

    prRampToSlope { |phase|
        var history = Delay1.ar(phase);
        var delta = phase - history;
        ^delta.wrap(-0.5, 0.5) 
    }

    prRampToTrig { |phase|
        var history = Delay1.ar(phase);
        var delta = phase - history;
        var sum = phase + history;
        var trig = (delta / sum).abs > 0.5;
        ^Trig1.ar(trig, SampleDur.ir)
    }
}

NS_PhasorParent : NS_AbstractPhasor {

    *new { |bus, slope|
        ^super.new.init(bus, slope)
    }

    init { |inBus, inSlope|
        var phasor, dPhase;
        // add an arg Buffer here...
        phasor = Phasor.ar(DC.ar(0), inSlope);
        dPhase = Delay1.ar(phasor);
        phasorBus = inBus;
        Out.ar(phasorBus.phaseBus, dPhase);
        Out.ar(phasorBus.trigBus, this.prRampToTrig(dPhase));
        Out.ar(phasorBus.slopeBus, this.prRampToSlope(phasor));
    }

    createChild { |bus, subDiv|
        ^NS_PhasorChild(bus, this, subDiv)
    }
}

NS_PhasorChild : NS_AbstractPhasor {
    var parent;
    var <>subDiv = 1;

    *new { |bus, parent, subDiv|
        ^super.new.subDiv_(subDiv).init(bus, parent)
    }

    init { |inBus, phasor|
        var phase = Phasor.ar(DC.ar(0), phasor.slope * subDiv, 0, 1);
        parent = phasor;
        phasorBus = inBus;
        Out.ar(phasorBus.phaseBus, phase);
        Out.ar(phasorBus.trigBus, this.prRampToTrig(phase));
        Out.ar(phasorBus.slopeBus, this.prRampToSlope(phase));
    }

    // subDivs can also be < 1
    createChild { |bus, subDivN|
        ^NS_PhasorChild(bus, parent, subDivN * subDiv)
    }

    //syncUpdate { |trig, newSubDiv|
    //    Demand.ar(trig, 0, Dbufrd(phasorBuf, subDivIndex))
    //}
}
