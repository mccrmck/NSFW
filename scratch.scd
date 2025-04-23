NSFW.servers['nsfw_0'].server.options.numOutputBusChannels;
NSFW.servers['nsfw_0'].strips[0][2].inSynth.get(\thru,{|g| g.postln});
NSFW.servers['nsfw_0'].strips[0][1].inSynthGate;
NSFW.servers['nsfw_0'].strips[0][0].setInSynthGate(0);
NSFW.servers['nsfw_0'].strips[1][3].inSink.module.synths;
NSFW.servers['nsfw_0'].strips[0][0].moduleArray[0].controls[3].object;
NSFW.servers['nsfw_0'].window.swapGrid.controls.do({ |c| c.value.postln });
NSFW.servers['nsfw_0'].window.swapGrid.controls[0].value_(1);
NSFW.servers['nsfw_0'].server.plotTree;
NSFW.servers['nsfw_0'].server.queryAllNodes;
NSFW.servers['nsfw_1'].server.scope;
NSFW.servers['nsfw_0'].server.freqscope;
NSFW.servers['nsfw_0'].server.meter;
NSFW.servers['nsfw_0'].server.peakCPU;
NSFW.servers['nsfw_0'].server.avgCPU;
NSFW.servers['nsfw_0'].server.numUGens;
NSFW.servers['nsfw_0'].strips.


n = NSFW().makeWindow
a = NSFW.servers['nsfw_0'].options.numChans
a = NSFW.servers['nsfw_0'].options.outChannels


SynthDescLib.global.browse
// NS_ControlSink needs to work taking busses in RingModIn, BufferPB, SamplePB, ec.
// shortLoops, repper, freeze should have same mix mechanism as SwellFB
// all of the above should start muted, button chooses to unmute
// what happens if I map two controls to the same OSC control? does the udpating get fucked? recursion?
// last8 (and others I presume) are not freeing synths stored at synths[1] (when paused)


6.collect({})
NSFW.servers['nsfw_0'].strips[1][3].slots[1].toggleVisible

NSFW.servers['nsfw_0'].outMixer[0].slots[0].synths[0].get(\freq,{|a| a.postln})
NSFW.servers['nsfw_0'].outMixer[0].slots[0].controls[2].value
NSFW.servers['nsfw_0'].outMixer[0].sends

s.inputBus
s.meter
{In.ar(s.inputBus.subBus(0))}.play
s.outputBus
s.inputBus
Bus.audio(s)

OSCFunc.trace(false)

i = (a: 0, b:1)
i.removeAt('j')
nil.free
i


NS_Transceiver.continuousQueue

g = NS_Control(\bypass, ControlSpec(0,1,'lin',1));
g.actionDict
g.actionDict['print']
g.addAction(\print,{|c| c.value.postln})
n = NS_ControlButton(g,["best","best"])
n.view.front
n.isListening.not
g.normValue = 0.5
g.value = 12347
f.round(0.01)

Rect.containsPoint

k = Knob()
k.front

Point
c = ControlSpec(0,7,\lin,1)

c.map(0.5)

"two\nFirst".bounds

1.wrap(0,2)


// View().setContextMenuActions() => exploit this!

// Menu().tearOff_(true) for ModuleList

/*
a = Server.default.inputBus 
a.numChannels

inputStack   = StackLayout( 
    *8.collect({ |i|
        NS_ServerInputView( nsServer.inputs[i] )
    })
);
*8.collect({ |i| 
    NS_LevelMeter("input: %".format(i))
    .value_(1.0.rand)
    .action_({ |lm|
        lm.toggleHighlight;
        inputStack.index_(i)
    })
})
*/
c = ControlSpec(0.001,0.1,0)
c.map(0.5)

n = NS_ServerOutMeter(4)
n = NS_LevelMeter("test")
n.view.front




// Flord Ekstasis
// open w/ glissandos up to pitches? w/ reverb, delay
// intro to last song: noisy -> D#altered chord


/* TOMORROW
- how can I add to/build upon loaded datasets?
- if you can, try the saveExtra/loadExtra functions! :)
- modules like MultiIn that take inbusses must change from strip.modGroup to strip.group
- loadRegressor bug: 'value_' not understood (I think it's calling nil.value_(0.5))
- bug: MLPMeter dragHandler -> when I drag a module across an MLPMeter, it reads the module string...I thought canReceiveDragHandler only evaluated on MouseOff?
- MultiIn buttons: should the play/mute graphics be reversed? What makes the most sense?
- what about a Benjolin version w/ continuous control instead of switches for the oscillator/filter SelectXs?
- if I do a routing matrix, do I still have drag and drop for modules like EnvFollower, a forthcoming Concat, etc.?
- check Benjolincontrol ranges -> how far are they from original? Maybe freqs get scaled with something other than \exp?
- NS_Control.resetToDefault/initValue...and then all gui elements can all this with a double-click
*/

//snippets - [\mix, \bypass controls, addActions, NS_Module initClass, .init, methods


[1,1,0,0].convertDigits(2) // 12
[0,1,1,0].convertDigits(2) // 6
[0,0,1,1].convertDigits(2) // 3
[1,0,0,1].convertDigits(2) // 9


// ControlSwitch, ControlMenu, ControlButton -> integers and floats as values...should probably be ints?
// MonoSumSend, SumSplay use NS_ControlMenu, but I think we need a better UI for choosing output Channels 
// MultiChannelTest has a number box thing, this should be abstracted, no?

// ang. above, must go through all modules that have one-state buttons (triggers) and test with osc messages sending 1 and 0 (should round, no?)

// do I need a descructor for GUI elements -> control.removeAction(\qtGui)
// find a better way to set the sizes of NS_ControlFader elements (text, slider, numBox)
// if I do NS_Style.textLight.red_(0.5) it changes the value in the event...must resolve!



// screenshot, could be nice for an icon
Image.fromWindow(view).setSize(w,h,resizeMode) // [ doNotScale, ignoreAspectRatio, keepAspectRatio, keepAspectRatioByExpanding ]

// NS_Ikeda: 8/16 knobs in a grid, each one triggers a sample on Touch, variations on sample via value
// OSC_EQ: canvas widget combining eq and a multiXY overlay...
// 3/5 band, with the custom-module splitting/joining the incoming/outgoing OSC-message
// must revisit FFT: zero padding is a good thing or no?
// module idea: ConCat, RingMod -> module that hosts another module, cross modulation with input signal (signal already on bus, earlier in the strip) * hosted module
// module: use FluidSineFeature to do a Freeze type thing, but then be able to offset the frequencies to move around (glissando, etc.) the chord?
// FitzHughNagumo
// Bjarni's Mix.fill(5, GrainFM(...)).fold2
// WeaklyNonlinear2
// ZeroXBufRd
// ZeroXBufWr

ScopeView
FreqScope
Stethoscope;
