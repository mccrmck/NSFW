s is the firtse line

NS_ServerHub.servers['nsfw_0'].server.options.numOutputBusChannels
NS_ServerHub.servers['nsfw_0'].strips[0][2].inSynth.get(\thru,{|g| g.postln})
NS_ServerHub.servers['nsfw_0'].strips[3][1].inSynthGate
NS_ServerHub.servers['nsfw_0'].strips[3][1].setInSynthGate(3)
NS_ServerHub.servers['nsfw_0'].strips.deepDo(2,{ |i| i.inSynthGate.postln; })
NS_ServerHub.servers['nsfw_0'].server.plotTree;
NS_ServerHub.servers['nsfw_0'].server.scope;
NS_ServerHub.servers['nsfw_0'].server.freqscope;
NS_ServerHub.servers['nsfw_0'].server.meter;
NS_ServerHub.servers['nsfw_0'].server.peakCPU;
NS_ServerHub.servers['nsfw_0'].server.avgCPU;
NS_ServerHub.servers['nsfw_0'].server.numSynths;

n = NSFW(OpenStageControl,[64])
// dark start - dynklank drone, benjolin (later cybernetic) interjections, gradually fx on voices
// dance first - keep drone, get more dense as they stop talking -> can I find ways to develop ShortLoops?
// duo - swell bass, rhythmic metallic benjolin tones, granular voices 
// worldbuilding/intro - field recordings, some bass swells maybe?
// faces - something "rhythmic"
// Recorder with circular buffer that is always recording -> can I play with overwriting, not recording, etc.?
// see if I can put that on an output track that has all three voices as input?






// 3 band EQ?
// envelope follower -> take it off the strip, add a InModuleSink to the Module Window
// panner? autopan, random pan, spectral panner (maybe brighter sounds deviate from center?)
// channel summer w/ hpf (for sending to subs, bl.a)

// ServerHub -> add a TextField to the input busses so I can write notes: Saxophone, Hakon, etc.
// when assigning "play/bypass" buttons, inSynthGate gets an extra 0 (first press doesn't register)...how to fix?
// test autotune! 
// test inSynthGate when releasing inputBusses

// ServerHub can have a trigger Sequencer w/ a dragsink that can be dragged to different Demand synths?

Shaper

NSFW.numInChans = 2
n = NS_ServerHub.boot
Env.shapeNumber('lin')


0.5 * 0.01
0.75 * 0.01


{ OnePole.ar(LFPulse.ar(40),0.999)}.plot(0.1)

(
w = Window("test");

w.layout_(
    HLayout(
        NS_XY(),
        NS_XY(),
        NS_XY(),
        NS_XY(),
        NS_XY(),
        NS_Fader()
    )

);

w.front
)



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



{ x = SinOsc.ar(120); x.sign - x}.plot(0.1)
InsideOut


n = NS_PolandFB()
n.free
s.meter
