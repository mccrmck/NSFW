
NS_ServerHub.servers['nsfw_0'].server.options.numOutputBusChannels
NS_ServerHub.servers['nsfw_0'].strips[0][2].inSynth.get(\thru,{|g| g.postln})
NS_ServerHub.servers['nsfw_0'].strips[2][3].inSynthGate
NS_ServerHub.servers['nsfw_0'].strips[2][2].setInSynthGate(5)
NS_ServerHub.servers['nsfw_0'].strips[1][3].inSink.module.synths
NS_ServerHub.servers['nsfw_0'].strips[1][3].inSink.module.synths
NS_ServerHub.servers['nsfw_0'].strips[1][0].inSynthGate
NS_ServerHub.servers['nsfw_0'].strips.deepDo(2,{ |strp,i| strp.inSynthGate.postln; if(i % 4 == 3,{" ".postln}) })
NS_ServerHub.servers['nsfw_0'].server.plotTree;
NS_ServerHub.servers['nsfw_0'].server.scope;
NS_ServerHub.servers['nsfw_0'].server.freqscope;
NS_ServerHub.servers['nsfw_0'].server.meter;
NS_ServerHub.servers['nsfw_0'].server.peakCPU;
NS_ServerHub.servers['nsfw_0'].server.avgCPU;
NS_ServerHub.servers['nsfw_0'].server.numUGens;

n = NSFW(OpenStageControl,[64]);

NS_ServerHub.boot;


NS_ServerHub.servers['nsfw_0'].server.sendMsg("/n_free",1415)

n = NS_EnvFollow()
n.free



/*
ROOM: 12 minutes
- dark beginning
    - start with drone after fade to black
	- no bass maybe (bring in)
	- bring in delay
    - do some sampling/processing
    $ polandFB - subSend - dynKlank - varDelay | gateGrains x 3... or ShortLoops x 3?
ROOM: 8 minutes
- dance first
    - add another element => benjolin, or something else noisy?
    - blackout on cue from Randiane
ROOM: 
- corner/wall tableau
    - field recordings
    - this would be nice with fbSwell...or Repper?
    $ bufferPB - gain - Freeze - shortLoops? // can get rid of gain if I amplitude match the field recordings
ROOM
- entangled trio
    - continue with field recordings
	- bass swells into reverb/delay?
    $ polandFB - envgen (welch) - tanh - lpg - vardelay 
ROOM: 10 minutes
- duet
    - speech is back
    - processing on Antonia's voice

ROOM: 10 minutes
- secrets
    - "I'll take the stairs" => fx on filed recordings
	- no more text
	- maybe some polandPB screams?
    - bass transforms into perc thumps
	- sound takes more space
- phrase/ritual
    - no text
    - maybe I can be more soloistic?
ROOM
- dark beginning again 
*/

// IMMEDIATE TO DO:
// maybe the bufferPB issue was with NS_Freeze? Check \ns_freezeTrig synth

// try to implement numChans.reciprocal.sqrt as a scaling factor for Freeze, Repper, etc. TEST FIRST
// change BufferPB so that it starts synth on init, play/bypass button triggers reset at start argument
// on load, BufferPB instantiates a synth that can't be accessed...maybe load function should also set \buffer arg?
// bufferPB should not be able to play unless buffers are loaded!, nil checks everywhere!
// check buffer dragSink -> is it freeing old buffers?

// scratchPB doesn't always free synths that happen too quickly, this blocks the bus
// it's possible other synths are doing the same thing

// Repper keeps exploding....why?? THIS IS IMPORTANT!!!
// I think this has to do with Repper setting inSynthGate_(1)!
// inSynthGate needs to be sorted => use booleans, logical/bitwise operators!?!
// when server is loaded, each channel should get the right inSynthGate value - I think the fact that the contorllers are not synced it what fucks it up
// add in mute to gate Grains? 
// shortLoops -> trig should also recalculate deviation frames
// fix inSynthGates on channelstrips -> controls get called on load, is this messing things up?
// scratchPB and Last8 still don't work sometimes...they are the same mechanism, must crack this...
// make a Buffer test class - two synths, recorder and playback; test in multichannel, test with fx before, after, etc.

// I think all the load functions should take an action argument so I can do CondVar scheduling?

// NS_MonoSumSend gets a trim fader
// make NS_Haas?
// NS_OutChannelStrip -> instead of a popup menu, make a | < | 0 | > | with two buttons that clip/wrap at the right place
// inChannelStrip needs some changes -> I think they're listed in Joplin?



// can the integrator work on input signals? Is it interesting?
// look at binShift from the old ternOP setup PV_MagShift
// PV_BinScramble



// velvet noise...I think. Can't quite figure out the decorrelation part yet though
{TDuty.ar(SampleRate.ir.reciprocal,0,Dxrand(0!18 ++ [1,-1],inf)) }.plot(0.01);

///

// 3/5 band EQ? BLowShelf - (MidEQ) - MidEQ - (MidEQ) - BHiShelf
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


// autopan
b = Buffer.readChannel(s,"/Users/mikemccormick/Desktop/GHOST samples/stereo/ciaccona.aiff",channels:[0])
(
SynthDef(\ns_autoPan,{
    var numChans = NSFW.numChans;
    // var sig = In.ar(\bus.kr, numChans);
    var sig = PlayBuf.ar(1,b,BufRateScale.kr(b),loop:1);
    var trig = FluidOnsetSlice.ar(sig,9,\thresh.kr(0.2));
    var amp = Amplitude.ar(sig * \trim.kr(0).dbamp,0.1,0.1).clip2;
    var rand = TRand.kr(-1,1,trig).lag(0.5);

    var pan = Select.kr(\which.kr(0),[\chan.kr(0), amp * rand.sign, rand ]);

    sig = NS_Pan(sig,numChans,pan/*.wrap2*/,\width.kr(2));
    // NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(0) )
    Out.ar(0,sig) 

}).add
)
x = Synth(\ns_autoPan,[])
x.set(\which,1,\chan,1.0.rand2)
x.set(\trim,12,\thresh,0.05)
s.meter


// NS_ISISBass
(
x = {
    //var sig = SinOsc.ar(\freq.kr(40) + ({ 0.1.rand2 } ! 8) );
    var sig = SinOsc.ar(\freq.kr(40) );
    sig = sig.asin;
    sig = ((sig - Delay1.ar(sig)) / 2pi) % 1;

    /* Process analyzed signal here */
    sig = AllpassC.ar(sig,0.2,\delay.kr(0.06) + \offset.kr(0),\decay.kr(0.2),\gain.kr(1));

    sig = (Phasor.ar(DC.ar(0), sig, 0.0, 1.0) * 2pi).sin;
    sig = LeakDC.ar(sig);
    sig = HPF.ar(sig,20);
    sig = BHiShelf.ar(sig,5000,1,-6);
    //sig = BandSplitter2.ar(sig,120);
    //sig = sig[0] + DelayC.ar(sig[1],0.2,Array.rand(8,0,0.005));
    sig = sig ! 16;
    sig = sig * -38.dbamp;
    Out.ar(16,sig.sum * 16.reciprocal);
    sig
   // sig ! 2;
}.play(NS_ServerHub.servers['nsfw_0'].server);
)

x.set(\freq,40,\delay,0.061,\decay,0.2,\gain,1)
(
fork{
    loop{
        x.set(\delay,0.05.rrand(0.07),\offset,0.001.rand2);
        Synth(\bass,[\freq,40, \width,0.01.rrand(0.1),\rq,0.01.rrand(0.05),\gain,10,\amp,0.008],NS_ServerHub.servers['nsfw_0'].server);
        2.rrand(3.0).wait;
    }
}
)
s.scope
s.freqscope
s.meter

n = NS_MultiIn()
n.free

// waveguide flute?
// mediapipe -> google
0
(
SynthDef(\ns_integratorFB,{
    var numChans = NSFW.numChans;
    var sig = Impulse.ar(0);

    sig = sig + LocalIn.ar(numChans);
    sig = Integrator.ar(sig,\coef.kr(0.99));

    sig = sig * ({ {Rand(-1,1) * 1000} ! numChans } ! numChans);
    sig = sig.sum;
    sig = LeakDC.ar(sig);
    sig = RLPF.ar(sig, MouseX.kr(250,8000),MouseY.kr(0.1,1));
    sig = sig.clip2;
    LocalOut.ar(sig);

    sig = sig * -48.dbamp;

    sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
    NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )

}).add
);

x = Synth(\ns_integratorFB,[\bus,0],NS_ServerHub.servers['nsfw_0'].server)
x.set(\coef,0.99)
x.free


NS_ModuleList.open

n = NS_RingModIn()
n.free


b = Buffer.alloc(s, 2048, 1);
c = Buffer.readChannel(s,"/Users/mikemccormick/Desktop/GHOST samples/stereo/ciaccona.aiff",channels:[0])


// make a RingModIn
// make last8 controls more like BufferPB -> XY with startPos + loop length, rate on fader maybe?
