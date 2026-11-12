NSFW.servers['nsfw_0'].strips[0][0].inSynth.get(\thru, { |g| g.postln})
NSFW.servers['nsfw_0'].strips[0][0].slots[0].gateBool
NSFW.servers['nsfw_0'].server.plotTree;
NSFW.servers['nsfw_0'].server.queryAllNodes
NSFW.servers['nsfw_0'].server.scope;
NSFW.servers['nsfw_0'].server.freqscope;
NSFW.servers['nsfw_0'].server.
NSFW.servers['nsfw_0'].printStats;

(
o = NS_ServerOptions(
    inChans: 4,
    inDev: "OCTA-CAPTURE",
    outDev: "OCTA-CAPTURE",
);

NSFW(o)
);


OpenStageControl.connect;
OpenStageControl.cleanUp;
OpenStageControl.drawView.front;
OpenStageControl.netAddr;
OpenStageControl.refresh;


NS_Test.run(false);



// Last8 can look like BufScratch
// work on SamplePB
// implement the FluidSines -> noisySines module pls.
// BufScratch: Buffer UGen: no buffer data when synthdef already loaded
// what the fuck is this Bus loading bug? I have temporary fixes atm, but this is kinda serious...

// Update NS_Pan - should it instead be Splay and SplayAZ? or more options? FluidSines needs this, many others
// when I click on Strip.showAll it should create one window with all modules!!


AkaiLPD8.connect;
MIDIFunc.trace;

(
p = "/Users/mccrmck/Desktop/GHOST\ samples/stereo/ciaccona.aiff";
//p = "/Users/mccrmck/Desktop/edgy\ palates\ sodas/01-241027_1907.wav";
c = NS_ControlString(\file, p);
w = Window().front;
w.layout_(
    HLayout(
        VLayout(
            *8.collect { |i| 
                NS_Button().addLeftClickAction({ n.currentSelection_(i) })
            }
        ),
        n = NS_Waveform(c)
    ).nsMarginsSpacing('view')
)
);

n = NS_ControlInt(\trig, 0, 1, 0).addAction(\s, {|c| c.value.postln })
n.value_(4)

s.boot


(
//var lfo = {
//	var trigger = Dust.ar(0.4);
//	Select.ar(ToggleFF.ar(trigger), [
//		LFNoise2.ar(TExpRand.ar(0.5, 10, trigger)),
//		TRand.ar(-1, 1, trigger),
//	]);
//};

var rungler = { |dataSignal, clockSignal|
    //var numBits, bits, bit, out, doDAC;
    var doDAC = { |bits|
        var result = bits[0] + (bits[1] * 2) + (bits[2] * 4);
        ((result / 7) * 2) - 1;
    };
    var bit = dataSignal > 0;
    var numBits = 8;
    var bits = [];
    numBits.do {
        bit = Latch.ar(bit, clockSignal);
        bits = bits.add(bit);
        bit = Delay1.ar(bit);
    };
    (
        out: doDAC.(bits[numBits - 3..numBits - 1]), // bits [6, 7, 8]
        outReverse: doDAC.(bits[numBits - 2..numBits - 4]), // bits [7, 6, 5]
    );
};

SynthDef(\rungler, {
    var pwm;
    var snd = LocalIn.ar(2);
    var runglerOut = rungler.(snd[0] * \scale.kr(1), snd[1]);
    snd = [runglerOut[\out], runglerOut[\outReverse]];
    snd = LFTri.ar([\freq1.kr(40), \freq2.kr(1200)] * (snd * 24).midiratio);

    LocalOut.ar(LeakDC.ar(Sanitize.ar(snd)));

    pwm = (snd[0] > snd[1]) - 0.5; // pwm
    //snd = MoogFF.ar(snd, \filtFreq.kr(500) * (runglerOut[\out] * 24).midiratio, 3);

    snd = SelectX.ar(\which.kr(0), [snd[0], snd[1], (snd[0] > 0) - 0.5, (snd[1] > 0) -0.5, pwm]);
    snd = SVF.ar(
        snd, (runglerOut['out'] * 24 * \runglerFilt.kr(1)).midiratio * \filtFreq.kr(1200), 1 - \rq.kr(0.5),
        \lowP.kr(1), \bandP.kr(0), \highP.kr(0), \notch.kr(0), \peak.kr(0),
        mul: \gain.kr(1)
    );
    Out.ar(\out.kr(0), snd);
}).add;
)


x = Synth(\rungler);
x.set(\filtFreq, 1200, \lowP,1, \highP,0, \bandP, 0, \rq, 0.2, \runglerFilt, 1)
x.set(\freq1, 200, \freq2, 100)
x.set(\which, 4, \scale, 1)
s.scope
s.freqscope

/*
## general
- make a .makeStripId function, keep it with the Strip classes

- consider subclassing BasicNodeWatcher; I use synth.register for dynamic
loading of synthdefs, but after boot calling NodeWatcher.all['nsfw_0'].nodes.dopostln
returns:

1052: Synth('ns_stripIn' : 1052)
1335: Synth('ns_inStripFader' : 1335)
1337: Synth('ns_inputMono' : 1337)
1016: Synth('ns_stripFader' : 1016)

...which is fine - but then all this garbage sits in NodeWatcher.nodes... 


*/

s.boot
{ QDFT.ar() }.play;
q = QDFTLang(200, 400, resolution: 12);
q.size.postln;
q.freqs.dopostln;
q.qualities.dopostln;
q.periods.dopostln;
q.periods[0].postln;
q.offsets.dopostln;
q.offsets[35] + q.periods[35];
q.inputs.size;
q.latencies.dopostln;
q.fiddles.flat.size;
q.outputs.dopostln;

x = 1024.collect { |i| i.linlin(0,1023,-pi,pi).sin }
g = q.analyzeSample(x).dopostln;
g = q.analyzeSample(Array.fill(q.size,{ 1.0.rand2 }));

/* ========== Farzane Nouri's Diagetic Operands Synth ================== */
/* ========== (this has been tidied, original is on Github ) ============ */
(
x = {
    var sig2;
    var sig3;
    var sig4;
    var freq4   = \freq4.kr(120);
    var clipNz  = LFDClipNoise.ar(2).range(0.02, 10);
    var foldVal = SinOsc.ar(freq4 * 0.001);

    var sig5, osc;
    var freq5 = \freq5.kr(100);

    var sig6;
    var freq6 = \freq6.kr(10);
    var ch = \ch.kr(0.3);
    var surface = \surface.kr(0.0001);

    var env = EnvGen.kr(Env.asr(7.8,3.2,3.6), \gate.kr(1), doneAction: 2);

    var freq1 = \freq1.kr(120);
    var sig1 = SinOscFB.ar([30,32], LFNoise1.ar(freq1).range(0.1,2.5));

    // all of the default arguments here seem completely off
    // especially considering this seems to be based on LPCError helpfile example
    // sig2 = Saw.ar(\freqLo.kr(100) * [1,0.9,0.8,0.7,0.5]);
    // sig2 = Resonz.ar(sig2, \freqHi.kr(1000), 0.1, 2);
    // sig2 = sig2 + LPCError.ar(sig2, 4.6); // add argument for filterCoefs 
    // sig2 = Splay.ar(sig2); // I added this
    // // what are these cutoffs? check the specs below, make changes
    // //sig2 = BLowPass4.ar(sig2, \modFreq.kr(0.01) * 900, 0.25, mul:0.5) + BHiPass4.ar(sig2, 2, 0.25, mul:0.5);
    // sig2 = sig2 * \amp2.kr(-120.dbamp);

    // this is one of Bjarni's pseudo-UGens...
    // sig3 = OFDagal.ar(57, rate:\rate.kr(0.05), resTo:1, lpf:100, mul:2) * \amp3.kr(-120.dbamp);

    //sig4 = PMOsc.ar(freq4, Lag.ar(clipNz, 0.01), [10, 1000]).fold2(foldVal) * \amp4.kr(-12.dbamp);
    // original cutoffRange was 0.1 <-> 23000; this has been changed
    //sig4 = HPF.ar(sig4, LFNoise1.ar(16).range(40, 13000), 0.3) * \amp4.kr(-12.dbamp);

    // sig5 = VarSaw.ar(
    //     LFPar.ar(freq5/[10,8], 0, 10, LFPar.kr(5, 0, 5, 40)),
    //     0,
    //     LFNoise2.kr(freq5/3, 0.5, 1.5)
    // ) * 0.6;
    // osc  = Splay.ar(Pulse.ar(Array.series(10, freq5 * 0.02, 5))) * Lag.kr(Amplitude.kr(sig5), 0.1);
    // sig5 = osc + BPeakEQ.ar(BBandStop.ar(sig5, LFNoise1.kr(0.5, 600, 800), 8), 80, 3, -2);
    // sig5 = HPF.ar(Splay.ar(sig5), 60) * LFNoise1.ar(0.8).range(0.3,1.1) * \amp5.kr(-30.dbamp);

    sig6 = FitzHughNagumo.ar(
        Impulse.kr( LFNoise1.kr([1, 2], freq6/5, freq6) ),
        surface.linlin(0.0, 1.0, 0.0001, 0.4),
        ch.linlin(0.0, 1.0, 0.0001, 0.4),
        ch.linlin(0.0, 1.0, 0.01, 3),
        surface.linlin(0.0, 1.0, 0.01, 4),
        SinOsc.kr(20, 0, 0.7), 
        SinOsc.kr(21, 0, 0.8)
    ) * 0.25;

    LeakDC.ar(sig6);
    //[sig1, sig2, sig3, sig4, sig5, sig6].sum * env;
}.play;
)

// specs[0]  = ControlSpec(0.01, 2,\lin);                    // freq
// specs[1]  = ControlSpec(0.000001, 0.01,\exp);             // amp1
// specs[2]  = ControlSpec(10, 1200,\lin);                   // freqHi
// specs[3]  = ControlSpec(0.005, 900,\exp);                 // freqLo
// specs[4]  = ControlSpec(0.0000000000001, 0.0000009,\exp); // amp2
// specs[5]  = ControlSpec(40, 90000,\lin);                  // modFreq
// specs[6]  = ControlSpec(0.0001, 0.09,\lin);               // amp3
// specs[7]  = ControlSpec(0.01, 130.09,\lin);               // rate 
// specs[8]  = ControlSpec(0.000001, 0.3,\exp);              // amp4
// specs[9]  = ControlSpec(30.1, 200.09,\lin);               // freq4
// specs[10] = ControlSpec(0.000001, 0.02,\lin);             // amp5
// specs[11] = ControlSpec(30.1, 1500.09,\lin);              // freq5
// specs[12] = ControlSpec(0.000001, 0.2,\exp);              // amp6
// specs[13] = ControlSpec(8.1, 20.09,\exp);                 // freq6


//to scale the ramps, try DelayC w/ scaling the slope
// if dTime == 0.5 period, slope * 2
// if dTime == 0.25 period, slope * 4, etc.
// delay to move the left edge, scaling output + clipping @ 1 should move the right edge
// CoinGate, Gate could be useful!

(
var b = b ?? { NS_PhasorBus(s) };
var c = c ?? { NS_PhasorBus(s) };
var d = d ?? { NS_PhasorBus(s) };
x = {
    var freq = 1000;
    var p = NS_PhasorParent(b, freq / SampleRate.ir);
    var p2 = p.createChild(c, 0.5);
    var p3 = p.createChild(d, 3);
    [p.phase, p.trig,
        p2.phase, p2.trig, p3.phase, p3.trig]
    }//.scope(6)
    .plot(0.0048).plotMode_(\dstems).plotColor_([Color.red.alpha_(0.5), Color.green.alpha_(0.5)])
)


(
// can use Dbufwr & Dbufrd for sample-accurate argument switching
// look into phase locked loops again...could this technique be used somehow?
x = {
    var trig = Impulse.ar([200,200]);
    var buf = LocalBuf(1);
    var bufWr = Dbufwr(Dseq([12000,1000]), buf, 0);
    var bufRd = Dbufrd(buf, 0);

    var write = Demand.ar(trig[0], 0, bufWr);
    var read = Demand.ar(trig[1], 0, bufRd);

    [trig[0], SinOsc.ar(read) * 0.2, trig[1].neg]
}.plot(0.01).plotMode_(\dstems).plotColor_([Color.red.alpha_(0.5), Color.blue.alpha_(0.5), Color.magenta.alpha_(0.5)]);
)

x.set(\freq, 300)
