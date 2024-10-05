
NS_ServerHub.servers['nsfw_0'].server.options.numOutputBusChannels
NS_ServerHub.servers['nsfw_0'].strips[0][2].inSynth.get(\thru,{|g| g.postln})
NS_ServerHub.servers['nsfw_0'].strips[3][1].inSynthGate
NS_ServerHub.servers['nsfw_0'].strips[3][1].setInSynthGate(3)
NS_ServerHub.servers['nsfw_0'].outMixer[0].pageIndex
NS_ServerHub.servers['nsfw_0'].strips.deepDo(2,{ |i| i.inSynthGate.postln; })
NS_ServerHub.servers['nsfw_0'].server.plotTree;
NS_ServerHub.servers['nsfw_0'].server.scope;
NS_ServerHub.servers['nsfw_0'].server.freqscope;
NS_ServerHub.servers['nsfw_0'].server.meter;
NS_ServerHub.servers['nsfw_0'].server.peakCPU;
NS_ServerHub.servers['nsfw_0'].server.avgCPU;
NS_ServerHub.servers['nsfw_0'].server.numUGens;

n = NSFW(OpenStageControl,[64])

// dark start - dynklank drone, benjolin (later cybernetic) interjections, gradually fx on voices
// dance first - keep drone, get more dense as they stop talking -> can I find ways to develop ShortLoops?
// duo - swell bass, rhythmic metallic benjolin tones, granular voices 
// worldbuilding/intro - field recordings, some bass swells maybe?
// faces - something "rhythmic"
// Recorder with circular buffer that is always recording -> can I play with overwriting, not recording, etc.?
// see if I can put that on an output track that has all three voices as input?

NSFW.numChans = 24
g = Group()
n = NS_DelayComp()
n.free

4.collect({ HLayout() }).clump(1)

MultiSliderView
ControlSpec
{x = 0; LFNoise1.kr(200).linexp(-1,1,x,1/x)}.plot(0.2)

NSFW.numChansd
///
//Check that the the buffers in yer new classes are initializing with the right number of channels

// 3 band EQ?
// envelope follower -> take it off the strip, add a InModuleSink to the Module Window
// panner? autopan, random pan, spectral panner (maybe brighter sounds deviate from center?)

// ServerHub -> add a TextField to the input busses so I can write notes: Saxophone, Hakon, etc.
// when assigning "play/bypass" buttons, inSynthGate gets an extra 0 (first press doesn't register)...how to fix?
// test autotune! 
// test inSynthGate when releasing inputBusses

// ServerHub can have a trigger Sequencer w/ a dragsink that can be dragged to different Demand synths?

Shaper

n = NS_ServerHub.boot


n = NS_Last16()
n.free

0.5 * 0.01
0.75 * 0.01


50 * 50

// waveguide flute?
// mediapipe -> google


// autopan
b = Buffer.readChannel(s,"/Users/mikemccormick/Library/Application Support/SuperCollider/Extensions/FluidCorpusManipulation/Resources/AudioFiles/Tremblay-BeatRemember.wav",channels:[0])
(
SynthDef(\ns_autoPan,{
    var numChans = NSFW.numOutChans;
    var sig = PlayBuf.ar(1,b,BufRateScale.kr(b),loop:1);
    var trig = FluidOnsetSlice.ar(sig,9,\thresh.kr(0.2));
    var pan = Amplitude.ar(sig * \trim.kr(0).dbamp,0.1,0.1).clip2;
    trig = TRand.kr(-1,1,trig).lag(0.2);

    sig = NS_Pan(sig,2,pan * trig,2);
    Out.ar(0,sig) 

}).add
)
x = Synth(\ns_autoPan,[])
x.set(\trim,15,\thresh,0.1)
s.meter


// new modules 
// granular delay w/ (snapping?) pitchShift; must be able to set rechead to 0 to create drones
// check out Nathan's multitap synth!
s.meter


// NS_ISISBass
(
x = {
   // var delay = MouseX.kr(0.01,0.2);
    //var decay = MouseY.kr(0.01,0.5);
    var sig = SinOsc.ar(\freq.kr(40) + { 0.01.rand2 } ! 8 );
    sig = sig.asin;
    sig = ((sig - Delay1.ar(sig)) / 2pi) % 1;

    /* Process analyzed signal here */
    sig = AllpassC.ar(sig,0.2,\delay.kr(0.06) + \offset.kr(0),\decay.kr(0.1),\gain.kr(1)).fold(0,1);

    sig = (Phasor.ar(DC.ar(0), sig, 0.0, 1.0) * 2pi).sin;
    sig = LeakDC.ar(sig);
    sig = HPF.ar(sig,20);
    sig = BHiShelf.ar(sig,5000,1,-6);
    sig = sig * -15.dbamp;
   // sig ! 2;
}.play(fadeTime: 0);
)

x.set(\freq,30,\delay,0.03,\decay,0.05,\gain,1)
(
fork{
    loop{
        x.set(\offset,0.001.rand2);
        2.wait;
    }
}
)
s.scope
s.freqscope
s.meter
