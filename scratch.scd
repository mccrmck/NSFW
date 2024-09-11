
NSFW.numOutChans

s.options.numOutputBusChannels

g = Group()
g.server.options.numOutputBusChannels

NS_ServerHub.servers['nsfw_0'].server.options.numOutputBusChannels
NS_ServerHub.servers['nsfw_0'].strips[0][2].inSynth.get(\thru,{|g| g.postln})
NS_ServerHub.servers['nsfw_0'].strips[1][2].inSynthGate
NS_ServerHub.servers['nsfw_0'].strips[1][2].setInSynthGate(0)
NS_ServerHub.servers['nsfw_0'].strips.deepDo(2,{ |i| i.inSynthGate.postln; })
NS_ServerHub.servers['nsfw_0'].window.swapGrid.controls[2].value;
NS_ServerHub.servers['nsfw_0'].inGroup.server;
NS_ServerHub.servers['nsfw_0'].server.plotTree;
NS_ServerHub.servers['nsfw_0'].server.scope;
NS_ServerHub.servers['nsfw_0'].server.freqscope;
NS_ServerHub.servers['nsfw_0'].server.meter;
NS_ServerHub.servers['nsfw_0'].server.peakCPU;
NS_ServerHub.servers['nsfw_0'].server.avgCPU;
NS_ServerHub.servers['nsfw_0'].server.numSynths;

n = NSFW(OpenStageControl,[64])
// duo - swell bass, metallic benjolin tones, granular voices 
// worldbuilding/intro - field recordings, some bass swells maybe?
// faces - something "rhythmic"

[0,7,12].midiratio
10.do({ Synth(\ns_inputMono) })
x.free
s.plotTree
Safety.disable

c = ControlSpec(0,32,\db)
c.map(0.25)

// graphic EQ
// 3 band EQ?
// envelope follower
// Saw synth w/ triads, can choose key and chords? Gmin
// autotune w/ key track
// Resonz w/ chromatic freqs + octave

// inSynth controls 
// gateThresh
// gain - \boostCut.asSpec
// EQ -> ability to add


n = NS_Vocoder()
n.free

NS_ServerHub.boot

NS_ModuleList.open

n = NS_Gate()
n.free

FluidSines

0.01.reciprocal.sqrt.ampdb

x = 2.reciprocal - 1

thresh + (signal - thresh) * ratio
(-10 + (-20 - -10).min(0)) * 2


(6 * -1).dbamp

// third octave bands
b = 
b.dopostln

FluidChroma

// rq = bandwidth/freq

(
SynthDef(\ns_graphicEQ,{
    var numChans = NSFW.numOutChans;

    var sig = In.ar(\bus.kr,numChans);
    var freqs = 20 * 30.collect({ |i| 2 ** (i/3) });
    var rq = 2 ** (-1/6);

    30.do({ |i|
        var name = \name;
        var boostCut = NamedControl.kr(name,0);
        sig = MidEQ.ar(sig, freqs[i], rq, boostCut )
    });

    Out.ar(0,sig)

}).add
)


Safety.disable

SawDPW



// dust/impulse trigger for repper? also -> fix repper amp/mix...
// if I can stop the pause script from clicking, make muting also pause ChannelStrips? To save CPU?
// ServerHub -> add a TextField to the input busses so I can write notes: Saxophone, Hakon, etc.
// BufferPB: can I add 6 (or whatever) DragSinks for audio files with a switch to go between them?
// when assigning "play/bypass" buttons, inSynthGate gets an extra 0 (first press doesn't register)...how to fix?





//
(
SynthDef(\ns_chorus,{
    var numChans = NSFW.numOutChans;
    //var sig = In.ar(\bus.kr, numChans);
    var in = PlayBuf.ar(2,\bufnum.kr,BufRateScale.kr(\bufnum.kr), loop: 1);

    var sig = in + LocalIn.ar(numChans);
    8.do{ sig = DelayC.ar(sig,0.2,{ SinOsc.kr(\rate.kr(0.1),{pi.rand}).range(0.01,0.025) }) };

    LocalOut.ar(sig * \feedB.kr(0.8));
    sig = sig + in;
    sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
    NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
}).add
)
b = Buffer.readChannel(s, "/Users/mikemccormick/Desktop/GHOST samples/stereo/ciaccona.aiff",channels:[0])
x = Synth(\ns_chorus,[\bus,0,\thru,1, \bufnum, b])
x.set(\rate,0.1,\feedB,0.2)
s.scope
s.meter


(
SynthDef(\ns_stripEnvFollow,{
    var numChans = NSFW.numOutChans;
    var sig = In.ar(\bus.kr, numChans);

    sig = sig * In.kr(\envComp.kr(1)); // should this be multichannel?!?!

    sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
    NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
}).add

)

/////


(
SynthDef(\ns_padSynth,{
    var numChans = NSFW.numOutChans;
    var freq = \freq.kr(80 * [0,4].midiratio);
    var sig = SawDPW.ar()


    sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
    NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
}).add
)

DegreeToKey

//
n = NS_Tanh()
n.free
s.scope
s.freqscope
s.meter
