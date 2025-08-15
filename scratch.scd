NSFW.servers['nsfw_0'].strips[0][2].inSynth.get(\thru,{|g| g.postln});
NSFW.servers['nsfw_0'].server.plotTree;
NSFW.servers['nsfw_0'].server.queryAllNodes;
NSFW.servers['nsfw_0'].server.scope;
NSFW.servers['nsfw_0'].server.freqscope;
NSFW.servers['nsfw_0'].server.meter;
NSFW.servers['nsfw_0'].printStats


NSFW.servers['nsfw_0'].inputs[0].controls.do({ |c| c.value.postln })
NSFW.servers['nsfw_0'].inputs[0].controls.last.value.c

n = NSFW().makeWindow;

"/state".pathMath

0.pathMath

c =  NS_Control('inBus', \string, "0").addAction(\test, { |c| c.value.postln })
d = NS_ControlText(c).front

c.value.asInteger



// 8 inputStrips, but must be able to choose which bus from all inputs!
// fix input Strip visibility - opening one strip closes others




// test save/load with complex routing between matrix strips, inputStrips
// Benjolin filters need to be reassessed - RHPF clicks on fast modulation
// their levels are also wildly different
// consider using SVF with different args


// scratchPB sucks
// Last8 and ScratchPB should be rethought
// should LPG have a freq offset control?
// repper, freeze, needs mix controls like swellFB/shortLoops (maybe they go to 6.dbamp?)


// can I get the widgets to .init their own mouseActionDict? Gotta test some inheritance BS...
// right click allows manual entry of value for knobs and faders
// samplePB -> sends signal when bypass is not on? Is this desired?

// must also check that loadExtra functions are returning properly
// what happens if I map two controls to the same OSC control? does the udpating get fucked? recursion?
// last8 (and others I presume) are not freeing synths stored at synths[1] (when paused)



/* TOMORROW
- how can I add to/build upon loaded datasets?
- loadRegressor bug: 'value_' not understood (I think it's calling nil.value_(0.5))
- bug: MLPMeter dragHandler -> when I drag a module across an MLPMeter, it reads the module string...I thought canReceiveDragHandler only evaluated on MouseOff?
- check Benjolincontrol ranges -> how far are they from original? Maybe freqs get scaled with something other than \exp?
*/

//snippets - [\mix, \bypass controls, addActions, NS_Module initClass, .init, methods

// MonoSumSend, SumSplay use NS_ControlMenu, but I think we need a better UI for choosing output Channels 
// MultiChannelTest has a number box thing, this should be abstracted, no?

// ang. above, must go through all modules that have one-state buttons (triggers) and test with osc messages sending 1 and 0 (should round, no?)


// screenshot, could be nice for an icon
Image.fromWindow(view).setSize(w,h,resizeMode) // [ doNotScale, ignoreAspectRatio, keepAspectRatio, keepAspectRatioByExpanding ]

// NS_Ikeda: 8/16 knobs in a grid, each one triggers a sample on Touch, variations on sample via value
// OSC_EQ: canvas widget combining eq and a multiXY overlay...
// 3/5 band, with the custom-module splitting/joining the incoming/outgoing OSC-message
// must revisit FFT: zero padding is a good thing or no?
// module idea: ConCat, RingMod -> module that hosts another module, cross modulation with input signal (signal already on bus, earlier in the strip) * hosted module
// module: use FluidSineFeature to do a Freeze type thing, but then be able to offset the frequencies to move around (glissando, etc.) the chord? have a lag parameter to glide betweeen Latch'ed chords, and then be able to transpose them as well...
// FitzHughNagumo
// Bjarni's Mix.fill(5, GrainFM(...)).fold2
// WeaklyNonlinear2
// ZeroXBufRd, ZeroXBufWr

b = Buffer.readChannel(s, "/Users/mccrmck/Desktop/GHOST samples/stereo/ciaccona.aiff", channels: [0])

(
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


10 * (2 ** (1 * 0))


(
{
    var freq = 200;
    var formantFreq = [500,800];
    var phase = Phasor.ar(DC.ar(0), freq * SampleDur.ir) * formantFreq / freq;
    phase;
    NS_UnitShape.hanningWin(phase.clip(0,1), 0.5)

}.plot(0.02);
)


b = Buffer.readChannel(s, "/Users/mccrmck/Desktop/GHOST samples/stereo/ciaccona.aiff", channels: [0])
s.boot
(
x = {
    var numChannels = 8;
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

    // could use a wavetable here, or run another phasor and multiply by windows
    var bufnum = \bufnum.kr(b);
    var frames = BufFrames.kr(bufnum);
    var pos = Phasor.ar(DC.ar(0), \rate.kr(1), \start.kr(0) * frames, \end.kr(1) * frames);
    var sig = BufRd.ar(1, bufnum, pos) * 6.dbamp;
    var win = NS_UnitShape.hanningWin(winPhase.clip(0,1), 0.5);
    sig = (win * sig).sum * ampComp;

   LeakDC.ar(sig) ! 2 * -9.dbamp
}.play
)

x.set(\start, 0.5, \end, 0.504, \rate, 1)



s.freqscope
s.scope
s.meter

GrainBuf
