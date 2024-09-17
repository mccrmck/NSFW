

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
// dark start - dynklank drone, benjolin (later cybernetic) interjections, gradually fx on voices
// duo - swell bass, rhythmic metallic benjolin tones, granular voices 
// worldbuilding/intro - field recordings, some bass swells maybe?
// faces - something "rhythmic"



// graphic EQ
// 3 band EQ?
// envelope follower -> take it off the strip, add a InModuleSink to the Module Window
// panner? autopan, random pan, spectral panner (maybe deviation from mean/center?)
// channel summer w/ hpf (for sending to subs, bl.a)

// dust/impulse trigger for repper? also -> fix repper amp/mix...
// if I can stop the pause script from clicking, make muting also pause ChannelStrips? To save CPU?
// ServerHub -> add a TextField to the input busses so I can write notes: Saxophone, Hakon, etc.
// BufferPB: can I add 4 (or whatever) DragSinks for audio files with a switch to go between them?
// when assigning "play/bypass" buttons, inSynthGate gets an extra 0 (first press doesn't register)...how to fix?
// test autotune! 
// test inSynthGate when releasing inputBusses

// inSynth controls 
// gateThresh
// gain - \boostCut.asSpec
// EQ -> ability to add/remove bands dynamically




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


0.5 * 0.01
0.75 * 0.01


{ OnePole.ar(LFPulse.ar(40),0.999)}.plot(0.1)

{a = LFTri.ar([420,240,80,200]); [a.sum, Mix(a)]}.plot
