
NS_ServerHub.servers['nsfw_0'].server.options.numOutputBusChannels
NS_ServerHub.servers['nsfw_0'].strips[0][2].inSynth.get(\thru,{|g| g.postln})
NS_ServerHub.servers['nsfw_0'].strips[0][0].inSynthGate
NS_ServerHub.servers['nsfw_0'].strips[0][0].setInSynthGate(0)
NS_ServerHub.servers['nsfw_0'].strips[1][3].inSink.module.synths
NS_ServerHub.servers['nsfw_0'].strips[0][0].moduleArray[0].controls[3].object
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


// multichannelTest doesn't work with 4 speakers...should be fixed now, must test
// maybe multiIn should not open the inSynthGate?
// EnvGen OSC interface is cramped..
// really have to go through inSynthGates for all modules -> repper especially
// make ShiftRegister OSC interface vertical instead of horizontal
// padSynth gets more than maj/min...also aug, dim?

n = NS_Freeze()
n.free

// to consider:
// in quad, pitchshift getting one channel of Freeze, sums, then expands to 4 chans?
// ringMod, however, only ringmods the one channel, no expansion
// this is due to the summing in PitchShift, which is for analysis, but...
// I need to go through all the summing modules and decide their behaviour
// for starters, I think PitchShift should analyze the sum, then pitch shift each channel accordingly
// change module behaviour -> window does *not* pop up on init, can then edit load function from channelStrips


NS_ServerHub.servers['nsfw_0'].server.sendMsg("/n_free",1415)
2.sign

200 * 3 * (7/9)

x = 60/100 * 0.75 // 1 bar
y = 60/70 * 3/5
z = (60/120)
p = (y + z)/4
p = x - y

q = 60/p

57.midicps / 8


FluidAudioTransport
/Users/mikemccormick/Downloads/Microtonal chords.wav

/Users/mikemccormick/Desktop/untitled.wav


(
e = Buffer.readChannel(s,"/Users/mikemccormick/Downloads/Microtonal chords.wav",channels:[0]);
d = Buffer.readChannel(s,"/Users/mikemccormick/Downloads/Guitar + Drums.wav",channels:[0]);
)

(
~synth = {
    arg windowSize = 1024, hopSize = 64, fftSize = 2048;
    var srcA = PlayBuf.ar(1,~srcA,BufRateScale.ir(~srcA),loop:1);
    var srcB = PlayBuf.ar(1,~srcB,BufRateScale.ir(~srcB),loop:1);
    FluidAudioTransport.ar(srcA,srcB,MouseX.kr,windowSize,hopSize,fftSize,16384).dup;
}.play
)

(
SynthDef("help-xfade2", { arg out=0, bufnumA=0, bufnumB=1, soundBufnum=2;
    var inA, chainA, inB, chainB, chain;
    //inA = Mix(Formant.ar([120, 180], [1200, 2000], [240, 360], 0.2));
    inA = PlayBuf.ar(1, \soundBufnumA.kr, BufRateScale.kr(\soundBufnumA.kr), loop: 1);
    inB = PlayBuf.ar(1, soundBufnum, BufRateScale.kr(soundBufnum), loop: 1);
    chainA = FFT(bufnumA, inA);
    chainB = FFT(bufnumB, inB);
    //chain = PV_XFade(chainA, chainB, MouseX.kr);
    chain = PV_Morph(chainA, chainB, MouseX.kr);
    Out.ar(out,  IFFT(chain).dup);
}).play(s,[\out, 0, \bufnumA, b.bufnum, \bufnumB, c.bufnum, \soundBufnum, d.bufnum,\soundBufnumA, e.bufnum]);
)


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
// test inSynthGate when releasing inputBusses

// ServerHub can have a trigger Sequencer w/ a dragsink that can be dragged to different Demand synths?

Shaper

n = NS_ServerHub.boot

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
b = Buffer.readChannel(s,"/Users/mikemccormick/Desktop/GHOST samples/stereo/ciaccona.aiff",channels:[0])

(
x = {
    //var sig = SinOsc.ar(\freq.kr(40) + ({ 0.1.rand2 } ! 8) );
    //var sig = SinOsc.ar(\freq.kr(40) );
    var sig = PlayBuf.ar(1,b, BufRateScale.kr(b),loop:1);
    sig = sig.asin;
    sig = ((sig - Delay1.ar(sig)) / 2pi) % 1;


    /* Process analyzed signal here */
    //sig = AllpassC.ar(sig,0.2,\delay.kr(0.06) + \offset.kr(0),\decay.kr(0.2),\gain.kr(1));
    sig = [sig, Slew.ar(sig,MouseX.kr(1,10000),MouseY.kr(1,10000))]; 
    //sig = BPF.ar(sig,MouseX.kr(80,8000).poll,MouseY.kr(0.1,1));

    sig = (Phasor.ar(DC.ar(0), sig, 0.0, 1.0) * 2pi).sin;
    sig = LeakDC.ar(sig);
    sig = HPF.ar(sig,20);
    sig = sig * -18.dbamp;
}.play//(NS_ServerHub.servers['nsfw_0'].server);
)
x.free
x.set(\freq,40,\delay,0.061,\decay,0.2,\gain,1)
s.meter
s.scope
s.freqscope

// waveguide flute?
// mediapipe -> google

b = Buffer.readChannel(s,"/Users/mikemccormick/Desktop/GHOST samples/stereo/ciaccona.aiff",channels:[0])
(
SynthDef(\ns_integratorFB,{
    var numChans = NSFW.numChans;
    //var sig = Impulse.ar(0);
    var sig = PlayBuf.ar(1,b,BufRateScale.kr(b),loop:1);

    sig = sig + LocalIn.ar(numChans);
    sig = Integrator.ar(sig,\coef.kr(0.99));

    sig = sig * ({ {Rand(-1,1) * 1000} ! numChans } ! numChans);
    sig = sig.sum;
    sig = LeakDC.ar(sig);
    //sig = DelayC.ar(sig,0.5,MouseX.kr(0.01,0.5));
    sig = RLPF.ar(sig, MouseX.kr(250,8000),MouseY.kr(0.1,1));
    sig = sig.clip2;
    LocalOut.ar(sig);

    sig = sig * -21.dbamp;

    sig = NS_Envs(sig, \gate.kr(1),\pauseGate.kr(1),\amp.kr(1));
    NS_Out(sig, numChans, \bus.kr, \mix.kr(1), \thru.kr(1) )

}).add
);

b.play

x = Synth(\ns_integratorFB,[\bus,0])
x.set(\coef,0.9)
x.free
s.plotTree
s.meter
s.scope
s.freqscope

// make last8 controls more like BufferPB -> XY with startPos + loop length, rate on fader maybe?
// part of the Repper overhaul should be making the three buttons into one switch
// go over GUI stuff - be consistent with order of arguments 
// NS_Switch loses orientation arg, get columns -> would be great to have odd number of buttons spread evenly over two rows, for example (4, 3)
// fix also default spacing on switches!! make it easier to scale/size them as well?
// OSC_Panel needs a grid/columns arg as well! Will greatly reduce the size of sent .json files
// when loading a server file, perhaps the filename should be visible somewhere?
TempoClock.default.tempo = 1 

60/100/3
60/75 / 4
60/120 / 2.5
60/48 * 4

60/72 * 6


60 * 0.8

(
c = ClickConCat(1,Click(100,4,2),Click(75,2,2),Click(120,2,2)).out_(4);
d = ClickConCat(1,Click(100,4,2),Click(75,2,2),Click(120,2,2)).out_(5);
//d = ClickConCat(1,Click(120,2,2),Click(75,2,2),Click(100,4,2)).out_(4);
//d = ClickConCat(1,Click(60,5,2)).out_(5);
//d = ClickConCat(1,Click(48,4,3)).out_(4);
//d = ClickConCat(1,Click(72,6,2)).out_(5);
p = Ppar([
    c.pattern,
    d.pattern,
],inf)
)
d.duration

p.play

// out 4: torfinn
// out 4: Oskar

(
var numT = 4;
var barDur = Array.rand(numT,0,1.0).normalizeSum * 12;
var tempo = 60/(barDur/4);
tempo.postln;
c = ClickConCat(inf,*numT.collect{ |i| 
    var bpm = tempo[i];
    var subD = if(bpm < 70,{2},{1});
    Click(bpm,4,subD,repeats:1) 
}).out_(5);
d = ClickConCat(inf,Click(100,5)).out_(4);
p = Ppar([
    c.pattern,
    d.pattern,
],1)
)


x = 60/100
12/x

c.duration

p.play
y = [ 169.91546272429, 30.412629325864, 113.3759492169, 414.34789931258 ]
j = ClickConCat(1, 4.collect{|i| Click(y[i],4)})
j.duration

b = (60/[170,30,130,414]).normalizeSum * 12
60/(b/4)

c = 60/[179,30,113,414] * 4
c.sum
60/ (c.sum/20)

s.meter
// 0: torfinn
// 1: kit
// 4: bDrum

s.meter

n = NS_RingMod()
n.free


