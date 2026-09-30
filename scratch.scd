NSFW.servers['nsfw_0'].strips[0][0].inSynth.get(\thru, { |g| g.postln})
NSFW.servers['nsfw_0'].strips[0][0].slots[0].gateBool
NSFW.servers['nsfw_0'].server.plotTree;
NSFW.servers['nsfw_0'].server.queryAllNodes;
NSFW.servers['nsfw_0'].server.scope;
NSFW.servers['nsfw_0'].server.freqscope;
NSFW.servers['nsfw_0'].server.meter;
NSFW.servers['nsfw_0'].printStats;

(
o= NS_ServerOptions();

NSFW(o)
)


NS_Test.run(false);


// add method for accessing modules easier:
NSFW.servers['nsfw_0'].moduleAt(0, 0, 0) // strip 
NSFW.servers['nsfw_0'].moduleAt("i", 0, 0) // inStrip 
NSFW.servers['nsfw_0'].moduleAt("o", 0, 0) // outStrip 


/* 
** STRIPS:
* finish routing implementation: receiveSlots, right click menus
* cleanup of *Strip classes, allow for adding DemandStrip, BufferStrip etc.
* find better class names, be consistent
**** OutStrip
* AbstractStrip.addSend should be overloaded maybe? 
** I'm never sending from an OutStrip to another *Strip
* maybe each OutStrip gets an array of LevelMeters as a routing interface
* click on a levelMeter, that + (numOutChans - 1) get highlighted
* ...but I would like to be able to route to multiple outs ie. 0-1 & 2-3
** so I can route to hardware and then to REAPER, for example
** GENERAL
* test saving/loading: modules, mapping, paused strips, etc.
** TRANSCEIVER
* functions need some love/clarity
* MIDI mapping, confirm OSC mapping works as expected
** SERVERWINDOW
* add controller window to main window
* add buffer synths/strips with NS_Waveform to top of GUI -> with routing!
** MLP
* work out regressor saving/loading
****
* NS_Messages have potential, keep going!
* consider a .register(Main.elapsedTime) method for scheduling
*/


// test ControlString, test saving symbols
n = NS_ControlFloat(\test, \db).addAction(\post, { |c| c.value.postln});
n.assignOSCcontroller('/path', NetAddr("localhost", 8080))

(
n = NS_ControlFloat(\test, \db);
//.addAction(\post, { |c| c.value.postln});
m = NS_ControlFloat(\test, \db, 0);
//.addAction(\post, { |c| c.value.postln});
w = Window().background_(Color.gray).front;
w.layout_(
    VLayout(
        NS_ControlFader(n, 0.01),
        NS_ControlFader(m, 0.01),
        NS_ControlRange(n, m,)
    )
)
)

a = \amp.asSpec
b = \db.asSpec
a.map(0.5).ampdb
b.map(0.5)
0.25.ampdb
NS_Transceiver.continuousQueue
NS_Transceiver.discreteQueue


AkaiLPD8.connect
MIDIFunc.trace

// Transceiver check: 
if(NS_Controller.allActive.size == 0) { 
    "no controllers active".warn 
}

// FIX:
// tried to map a ControlFloat to a Switch
// oscLastPath is annoying
// NS_Waveform needs markers (w/ manual entry), can they send triggers?!
// check harddrives for magnus' audio files (poem, refusalIntro/outro)?

/*
* NS_Control, NS_ControlDict, NS_ControlModule: add .free and .clear methods
* short and quick ideas:
* - `synths` should become a dictionary instead of an array: indexing feels
* like it will be flimsy with modules that have many synths
* (like our forthcoming LFO matrix)
*
*== REMEMBER/CONSIDER:
*- module windows can have tabs!
*- meaning (depending on how practical) modules with many controls can spread
*them across several pages/tabs
*- could be useful to also "hide" controls that can't/shouldn't be accidentally pressed
****
* LevelMeter.highlight is only used for selecting inputs - maybe rethink this
* could the LevelMeters be stacked vertically and expand to 
*/


(
p = "/Users/mccrmck/Desktop/GHOST\ samples/stereo/ciaccona.aiff";
//p = "/Users/mccrmck/Desktop/edgy\ palates\ sodas/01-241027_1907.wav";
c = NS_ControlString(\file, p);
w = Window().front;
w.layout_(
    HLayout(
        VLayout(
            *8.collect{ |i| NS_Button().addLeftClickAction({ n.currentSelection_(i) }) }
        ),
        n = NS_Waveform(c)
    ).nsMarginsSpacing('view')
)
);


n.currentSelection_(0)
n.sfView.selections[..7].dopostln

/* ======= TO: DO ======= 

## general
- maybe swap grid becomes NS_PageSwitch?
- make a .makeStripId function, keep it with the Strip classes
- many/most controlWidgets have .beginDragAction_({ control }); what is this for?

- consider subclassing BasicNodeWatcher; I use synth.register for dynamic
loading of synthdefs, but after boot calling NodeWatcher.all['nsfw_0'].nodes.dopostln
returns:

1052: Synth('ns_stripIn' : 1052)
1335: Synth('ns_inStripFader' : 1335)
1337: Synth('ns_inputMono' : 1337)
1016: Synth('ns_stripFader' : 1016)

...which is fine - but then all this garbage sits in NodeWatcher.nodes... 

//-----------------------------------------------//
## strip
- check pausing behaviour with Nodes in groups
- maybe I only need to trigger the pause gate of the strip and not each individual node?
- ...and maybe I only need to set stripGroup.run(false) and not any of the inner groups?
- maybe check if buffer playback continues in a synth while enclosing group is paused?

## outStrip
- no receives OR eight receives from inStrips
- numSends = outChans - (numChans - 1) (ie. 4 - 1, 8 - 3)
- creates numSends RoutingNodes in View
- clicking node0 also toggles numChans Nodes?
- this would prevent me from routing to 0-1 and 1-2, for example

//-----------------------------------------------//
## make ModuleSlotView a bit more composable:
- inStrips can't send moduleFragments to o-s-c, this will throw an error
- if I want modules that can host other modules, they can't send gui info to o-s-c
- hosting modules will want other info in a contextMenu; actions are handled by NS_Control

- get consistent about NS_Control keys - are they strings or symbols?!
- new NS_Style syntax (go via *doesNotUnderstand): NS_Style.bGroundDark, etc.

- move controlContextMenu to Control classes
- NS_Window should have a `armAllControlsForMapping` in header

- new startup: load from saved file, run headless, etc.
- headless will also be important for running (integration) tests!
- what about constructing a setup from text:
NSFW.newFrom(
    inStrips: (),
    strips: (
        // strip 0
        0: (
            // pages
            0: [NS_Olarte, NS_RingMod, nil, nil, nil, NS_VarDelay],
            2: [NS_Benjolin, NS_AmpMod, NS_Decimator, nil, nil, nil],
        ),
        // strip 0
        1: (
            // pages
            0: [NS_Pulsar, nil, nil, nil, nil, nil]
        )
    ),
    outStrips: ()
)

## StripRegressor
- test a lot, some bug appeared when demoing for Oskar...
- seemed like data points weren't getting sent to the DataSet? Or a size mismatch
- change adding three points to just adding 1? Consider two methods, addPoint, addPointCluster
- regressor has to be populated before it can load a model, can I automate this?
- when switching to a fresh mlp and predict == true, make predict == false
- do I need to save extra, to confirm the right modules are on the strip?
- also needs visual feedback for when MLPs are cleared/reset!

## some things didn't work on save, loading with Edgy Palates:
- Freeze was blowing up
- repper seemed not to work at all
- autotune also crapped out 
- samplePB was messed up
- what's the common thing here? Could be several problems: maybe loading buffers. maybe input gates aren't opening?
- also: o-s-c interface would sometimes not fully load, especially when both tablets were already connected
- check soundcard w/ REAPER, seems like "linking" two channels introduces crosstalk?
- check the inputs in general, see if there's any distortion, etc.
- maybe add a marker in the inStripMeters for a decent input level to meet the synths

## channelStrips
- check routing between channelStrips - is there a volume drop?
- test save/load with routing between channelStrip -> maybe that's where volume drop happens?
- sends/routing interface: right-click modal windows, clicks, color coding

## modules
- autotune gain compensation
- bufferPB: adding buffer plays immediately, should also change \whichBuf
- can use opt-click to clear a buffer, but it should then stop playing
- dynKlank should be so much better -> better interface, more efficient, etc.
- updateEnvGen to use Phasor based triggering, should solve the revPerc bug as well
- NS_Mute opens stripGate by default - I think this is necessary, should be documented though
- what is ScratchPB even doing? Make it better plz
- decimator gain compensation (should be correlated with bit depth)
- samplePB needs to resolve the bypass stuff
- Last8 and ScratchPB buffer stuff should be reconsidered
- should LPG have a freq offset control?
- frequency scaling in HenonSine -> range slider to set lo - hi freq
- could also consider freq input as part of another synthesis alogrithm
- last8 (and others I presume) are not freeing synths stored at synths[1] (when paused and then freed)

## widgets/GUI
- right click allows manual entry of value for knobs and faders
- knobs should also look better and not crowd the label/value text
- could also add trackpad scrolling == increment/decrement for faders/knobs
- multiple state NS_Button: add wrapping to .value method, requires storing stateSize as instance variable
- choose better method names for NS_Controllers and all subclasses

## miscellaneous
- check input volume from inStrip to channels (page 2)
- must also check that loadExtra functions are returning properly
- are there issues with mapping two controls to the same o-s-c message?
- implement versioning so I can load older saved sessions?

## bugs
- when deleting an active server, opened module windows don't get closed

## tests
- unit tests for all widgets
- unit tests for views

// screenshot, could be nice for an icon
resizeMode: [doNotScale, ignoreAspectRatio, keepAspectRatio, keepAspectRatioByExpanding]
Image.fromWindow(view).setSize(w,h,resizeMode);

*/

s.boot
{ QDFT.ar() }.play;
q = QDFTLang(20, 200, resolution: 12);
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


// ODE Ugens with Euler, RK2, RK4 solving: Pendulum2, FitzHugh-Nagumo, TermanWang, LotkaVolterra Predator-Prey, Duffing equation
// NS_Ikeda: 8/16 knobs in a grid, each one triggers a sample on Touch, variations on sample via value
// OSC_EQ: canvas widget combining eq and a multiXY overlay...
// 3/5 band, with the custom-module splitting/joining the incoming/outgoing OSC-message
// must revisit FFT: zero padding is a good thing or no?
// module idea: ConCat, RingMod -> module hosts another, modulates with input signal on strip
// above: maybe o-s-c gui is the hosted module, ringMod/concat algos don't have many parameters anyways
// FitzHughNagumo
// Bjarni's Mix.fill(5, GrainFM(...)).fold2
// WeaklyNonlinear2
// ZeroXBufRd, ZeroXBufWr
// sheppard tones, risset rhythms -> maybe using crossfading buffers playing at different speeds?
// could I use a NN to learn how to generate cool sounds from data files? Like datamoshing,
// revisit PolandFB synth -> the recording sounds pretty good, and quite different from what I have now...
// consider making a version that also writes input to the buffer instead of the wavetable? Could make for some cool shit?
// blotar? Half guitar/half flute...see here: "https://github.com/Cycling74/percolate/blob/master/source/projects/blotar~/blotar~.c"
// WalshHadamard, FFT-like deconstruction w/ pulses
// Paul Miller's ImageSynthApp: "https://github.com/txmod/TXImageSynthApp/tree/main"


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
