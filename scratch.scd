NSFW.servers['nsfw_0'].strips[0][2].inSynth.get(\thru,{ |g| g.postln });
NSFW.servers['nsfw_0'].server.plotTree;
NSFW.servers['nsfw_0'].server.queryAllNodes;
NSFW.servers['nsfw_0'].server.scope;
NSFW.servers['nsfw_0'].server.freqscope;
NSFW.servers['nsfw_0'].server.meter;
NSFW.servers['nsfw_0'].printStats

n = NSFW().makeWindow;




// make an NS_Modal/NS_PopOver?
// can use this to close window when clicking outside:
// (
// w = Window().front;
// w.view.endFrontAction_({ "test".postln; w.close });
// )
// and mouseEnter/mouseLeave for tooltips/hover boxes

/* ======= TO: DO ======= 
## StripRegressor
- test a lot, some bug appeared when demoing for Oskar...
- seemed like data points weren't getting sent to the DataSet? Or a size mismatch
- fix saving and loading MLPs
- change adding three points to just adding 1? Consider two methods, addPoint, addPointCluster

## inputs
- check input gain with test tone from REAPER? Or loopback from soundcard

## channelStrips
- check routing between channelStrips - is there a volume drop?
- test save/load with routing between channelStrip -> maybe that's where volume drop happens?
- show button on can be stateless!
- change to VLayout(volumeFader, showAll, Mute) 

## modules
- confirm Freeze and Repper volume makes sense
- scratchPB sucks
- Last8 and ScratchPB buffer stuff should be reconsidered
- should LPG have a freq offset control?
- Benjolin filters need to be reassessed - RHPF clicks on fast modulation, levels vary, maybe just use SVF
- frequency scaling in HenonSine -> range slider to set lo - hi freq
- MonoSumSend, SumSplay use NS_ControlMenu, but I think we need a better UI for choosing output Channels 
- MultiChannelTest has a number box thing, this should be abstracted, no?

## widgets/GUI
- can I get the widgets to .init their own mouseActionDict? Test inheritance... 
- right click allows manual entry of value for knobs and faders
- could also add scrolling increment/decrement for faders/knobs
- look at: NS_Widget.subclasses.dopostln; NS_ControlWidget.subclasses.dopostln;
- review these classes and their subclasses: do all NS_Widget subclasses need click functions?
- maybe we need NS_View classes, then NS_Widget -> NS_ControlWidget
- multiple state NS_Button: add wrapping to .value method, requires storing stateSize as instance variable
- choose better method names for NS_Controllers and all subclasses
- ChannelStripOut has ControlButtons for routingn to hardware outs -> should these be mappable?

## miscellaneous
- samplePB needs to resolve the bypass stuff
- check input volume from inStrip to channels (page 2)
- should I add mute butttons to ChannelStrip Inputs? and how would these appear in the o-s-c GUI?
- Consider adding a SpeedLimit Class (a la spluta) for incoming OSC messages?
- must also check that loadExtra functions are returning properly
- what happens if I map two controls to the same OSC control? does the udpating get fucked? recursion?
- last8 (and others I presume) are not freeing synths stored at synths[1] (when paused and then freed)

*/




{ x = QDFT.ar() }.play
q = QDFTLang(320, 640, resolution: 12)
q.size.postln
q.freqs.dopostln
q.qualities.dopostln
q.periods.postln;
q.periods[0].postln;
q.offsets[35] + q.periods[35]
q.inputs.size
q.latencies.dopostln
q.fiddles.flat.size
q.outputs.dopostln

g = q.analyzeSample(0.4)
g.postln
g = q.analyzeSample(Array.fill(q.size,{ 1.0.rand2 }))
g.dopostln
q.inputs.dopostln
q.outputs.dopostln
g.dopostln



//snippets - [\mix, \bypass controls, addActions, NS_Module initClass, .init, methods

// screenshot, could be nice for an icon
Image.fromWindow(view).setSize(w,h,resizeMode) // [ doNotScale, ignoreAspectRatio, keepAspectRatio, keepAspectRatioByExpanding ]

// NS_Ikeda: 8/16 knobs in a grid, each one triggers a sample on Touch, variations on sample via value
// OSC_EQ: canvas widget combining eq and a multiXY overlay...
// 3/5 band, with the custom-module splitting/joining the incoming/outgoing OSC-message
// must revisit FFT: zero padding is a good thing or no?
// module idea: ConCat, RingMod -> module that hosts another module, cross modulation with input signal (signal already on bus, earlier in the strip) * hosted module
// FitzHughNagumo
// Bjarni's Mix.fill(5, GrainFM(...)).fold2
// WeaklyNonlinear2
// ZeroXBufRd, ZeroXBufWr
// sheppard tones, risset rhythms -> maybe using crossfading buffers playing at different speeds?
// could I use a NN to learn how to generate cool sounds from data files? Like datamoshing,
// but a bit more sophisticated...data preprocessing might be important here, as with diffusion models
// revisit PolandFB synth -> the recording sounds pretty good, and quite different from what I have now...
// consider making a version that also writes input to the buffer instead of the wavetable? Could make for some cool shit?

b = Buffer.readChannel(s, "/Users/mccrmck/Desktop/GHOST samples/stereo/ciaccona.aiff", channels: [0])

(
// sine decomposition, interpolating between states
SynthDef(\sineDrone,{
    var bufnum = \bufnum.kr;
    var sig = PlayBuf.ar(1,bufnum,BufRateScale.kr(bufnum), loop:1);
    var chain = FFT(LocalBuf(2048),sig);
    var trig = \trig.tr;//Onsets.kr(chain, \thresh.kr(0.6));
    var sines = Latch.kr(FluidS0neFeature.kr(sig, 20), trig);

    var sineSig = LFTri.ar(sines[0].lag(0), mul: sines[1].lag(1)).sum;

    sig = [sig, sineSig];

    Out.ar(0, sig)

}).add
)

x = Synth(\sineDrone,[\bufnum, b])
x.set(\trig, 1)
s.meter
s.freqscope



(
{
    var freq = 1;
    var rDuty = 0.8;
    var phase = Phasor.ar(DC.ar(0), freq * SampleDur.ir) * rDuty;
    NS_UnitShape.gaussianWin(phase.clip(0,1), \skew.kr(0.5), 1)

}.plot(2);
)


b = Buffer.readChannel(s, "/Users/mccrmck/Desktop/GHOST samples/stereo/ciaccona.aiff", channels: [0])
s.boot
(
x = {
    var numChannels = 4;
    var freq        = MouseX.kr(4, 400, 1);
    var formantFreq = MouseY.kr(4, 1600, 1);
    var overlap     = freq / formantFreq;
    var maxOverlap  = min(overlap, numChannels);
    var phase       = Phasor.ar(DC.ar(0), freq * SampleDur.ir);// * formantFreqs / freq;
    var trig        = NS_GrainFuncs.rampToTrig(phase);
    var slope       = NS_GrainFuncs.rampToSlope(phase);
    var mChanTrig   = NS_GrainFuncs.mChanTrigger(numChannels, trig);
    var subS        = NS_GrainFuncs.subSampleOffset(phase, slope, mChanTrig);

    var mChanAccum = NS_GrainFuncs.mChanAccumSubSample(mChanTrig, subS);
    var slopes     = Latch.ar(slope, mChanTrig) / max(0.001, maxOverlap);
    var winPhase   = slopes * mChanAccum;
    var ampComp    = max(1, overlap).reciprocal.sqrt;

    //var sig = NS_UnitShape.hanningWin(winPhase.clip(0,1), 0.5).sum;
    // var sig = NS_UnitShape.tukeyWin(winPhase.clip(0,1), 0.5, 0.5, 0.5).sum;
    var sig = BufRd.ar(1, b, winPhase.clip(0,1) * BufFrames.kr(b), interpolation: 4);

    sig = sig.sum * ampComp;
    LeakDC.ar(sig) ! 2 * -9.dbamp
}.play
)

x.set(\start, 0.5, \end, 0.504, \rate, 1)

(
b = Buffer.alloc(s, 512, 1, { |buf| 
    buf.setnMsg(0,
        Array.fill(512, { |i| 
            sin(i/511 * 2pi) * 
            sin(i*20/511 * 2pi)
        })
    )
})
)


(
b = Buffer.alloc(s, 512, 1, { |buf| 
    buf.setnMsg(0, Array.fill(512, { |i| (i/511 * 2pi).cos }) )
})
)
b.plot


/* ================Farzane Nouri's Diagetic Operands Synth ======================== */
/* ============ (this has been tidied, original is on Github ) ==================== */

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
    // this line is in the original synth, but doesn't do anything
    // sig1 = Fold.ar(sig1, freq1, SinOsc.ar([0.03,0.1]).range(0.5,12.0)); * \amp1.kr(-120.dbamp);

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
    ) * 0.4;

    sig6 = BHiPass4.ar(sig6 * 0.3, 500);
    sig6 = LPF.ar(sig6, 16000) * \amp6.kr(-12.dbamp);

    LeakDC.ar(sig6);
    //[sig1, sig2, sig3, sig4, sig5, sig6].sum * env;
}.play;

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
