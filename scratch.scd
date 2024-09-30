
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
NS_ServerHub.servers['nsfw_0'].server.numSynths;

n = NSFW(OpenStageControl,[64])
// dark start - dynklank drone, benjolin (later cybernetic) interjections, gradually fx on voices
// dance first - keep drone, get more dense as they stop talking -> can I find ways to develop ShortLoops?
// duo - swell bass, rhythmic metallic benjolin tones, granular voices 
// worldbuilding/intro - field recordings, some bass swells maybe?
// faces - something "rhythmic"
// Recorder with circular buffer that is always recording -> can I play with overwriting, not recording, etc.?
// see if I can put that on an output track that has all three voices as input?

n = NS_FreeVerb()
n = NS_Tanh()
n.free


c = ControlSpec(0.1,36,1.5)
c.map(0.25)

//Check that the the buffers in yer new classes are initializing with the right number of channels


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

n = NS_ServerHub.boot
Env.shapeNumber('lin')


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

b = Buffer.readChannel(s,"/Users/mikemccormick/Library/Application Support/SuperCollider/Extensions/FluidCorpusManipulation/Resources/AudioFiles/Tremblay-BeatRemember.wav",channels:[0])
(
SynthDef(\ns_grainDelay,{
    var numChans = NSFW.numOutChans;
    //var sig      = In.ar(\bus.kr,numChans);
    var trig = Impulse.ar(\tFreq.kr(4));
    var grainDur = \grainDur.kr(1);
    var pos = Phasor.ar(DC.ar(0),SampleRate.ir.reciprocal,0,1);
    
    var sig = GrainBuf.ar(numChans,trig,grainDur,b,\rate.kr(1),pos,);

    sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
    NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
}).add;
)
x = Synth(\ns_grainDelay,[\thru,1])
x.set(\tFreq,2,\grainDur,1,\amp,0.5)

s.meter
b.play


{ x = LFDNoise3.ar(100,).range(1,1.01); x * 0.5 }.plot(0.1)
