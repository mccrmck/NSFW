
NSFW.servers['nsfw_0'].server.options.numOutputBusChannels
NSFW.servers['nsfw_0'].strips[0][2].inSynth.get(\thru,{|g| g.postln})
NSFW.servers['nsfw_0'].strips[0][1].inSynthGate
NSFW.servers['nsfw_0'].strips[0][0].setInSynthGate(0)
NSFW.servers['nsfw_0'].strips[1][3].inSink.module.synths
NSFW.servers['nsfw_0'].strips[0][0].moduleArray[0].controls[3].object
NSFW.servers['nsfw_0'].window.swapGrid.controls.do({ |c| c.value.postln })
NSFW.servers['nsfw_0'].window.swapGrid.controls[0].value_(1)
NSFW.servers['nsfw_0'].server.plotTree;
NSFW.servers['nsfw_0'].server.queryAllNodes;
NSFW.servers['nsfw_0'].server.scope;
NSFW.servers['nsfw_0'].server.freqscope;
NSFW.servers['nsfw_0'].server.meter;
NSFW.servers['nsfw_0'].server.peakCPU;
NSFW.servers['nsfw_0'].server.avgCPU;
NSFW.servers['nsfw_0'].server.numUGens;
NSFW.servers['nsfw_0'].window

n = NSFW(OpenStageControl,[64]);
n = NSFW(nil,[64])

n = NS_ModuleList()
n.front
n.close

(
w = Window().layout_(
    HLayout(
        ~uView = UserView().background_(Color.rand).layout_(
            GridLayout()
        )
    )
).front
)

x = [[0,1,2,3],[0,1,2,3],[0,1,2,3],[0,1,2,3]]
y = x.clump(2)
y[0]


n = NSFW().makeWindow
n = NSFW.servers
n.servers['nsfw_0'].pageGroups
n.servers['nsfw_0'].server.inputBus
n.servers['nsfw_0'].server.isRunning

s.inputBus
s.meter
{In.ar(s.inputBus.subBus(0))}.play
s.outputBus
s.inputBus
Bus.audio(s)


(
g = Group();
n = NS_ChannelStrip1(g,8);
r = NS_ChannelStrip1(g,3);
w = Window().layout_(VLayout(
    NS_ChannelStripView(n),
    NS_ChannelStripView(r),
)).front
)





s.plotTree
n = r
n.view.front
Server.killAll

(
var func = { |view, x, y, modifiers, xDelta, yDelta|
    var oldX = view.bounds.left;
    var oldY = view.bounds.top;
    var newY = oldY + yDelta;
    v.moveTo(oldX, newY);
    [xDelta, yDelta].postln;
    true
};

v = View().background_(Color.rand);
v.acceptsMouseOver = true;
v.mouseWheelAction_(func);
v.layout_(
    HLayout(
        View().background_(Color.blue)
        .mouseWheelAction_( func ).layout_(
            VLayout(
                *30.collect({ 
                    Knob()
                    .background_(Color.rand)
                    .mouseWheelAction_({ false });
                })
            )
        )
    )
);
v.front
)
v.bounds
v.moveTo(12,12)

678


n = NS_ServerOptions()
n.options
Server.allRunningServers

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

Server.default.outputBus
s.meter
NSFW.servers['nsfw_0'].free


(
w = Window().background_(Color.black).layout_(
    HLayout(
        Slider().thumbSize_(3).knobColor_(Color.clear),
        Button(),
        Knob()
    )
).alpha_(0.8).front;
w.view.deleteOnClose_(true)
)

w.view.children
w.layout.remove
~stack.insert(Slider2D(),2)
~stack.index_(1)
w.view.children
(
var tmp = ~stack.index;
~stack.index_(0);
w.view.children.reverse[0].remove;
~stack.index_(tmp)
)
~stack.count.postln

b = Dictionary()
b.put(\a, "")
b[\a].class
b.size
Order

blockSize

(
l = ListView().items_((1..24)).front;
l.beginDragAction_({ |lv| lv.items[lv.value] }).mouseDownAction_({ |v| v.beginDrag })
)
l.selection
l.value
(0..4).reject({ |i| i == 2})
l.items_(["tast"])
l.refresh

2 ** 6

4.nextPowerOfTwo

//ERROR
//2025-03-22 18:54:58.139 sclang[61375:217896] The class 'NSSavePanel' overrides the method identifier.  This method is implemented by class 'NSWindow'


// open w/ glissandos up to pitches? w/ reverb, delay
// intro to last song: noisy -> D#altered chord


OpenStageControl.boot
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
// entire ServerHub/inputmodule needs a rework, also the graphic eq

// do I need a descructor for GUI elements -> control.removeAction(\qtGui)
// find a better way to set the sizes of NS_ControlFader elements (text, slider, numBox)
// serverWindow can *not* be closed by user, has a quit button with pop up "Are you sure/SAVE?"
// if I do NS_Style.textLight.red_(0.5) it changes the value in the event...must resolve!


(
~spec1 = \freq.asSpec;
~spec2 = \db.asSpec;
~model = (freq: 440, amp: -9);
~controller = SimpleController( ~model );
~controller.put(\freq,{ |theChanger, what ...moreArgs| // ~model, \key, otherArgs
    ~view1.value_( ~spec1.unmap(theChanger[\freq]) )
});
~controller.put(\amp,{ |theChanger, what ...moreArgs| 
    ~view2.value_( ~spec2.unmap(theChanger[\amp]) ) 
});

~win = Window("test").front;
~view1 = Slider(~win,Rect(0,0,200,200)).action_({ |sl|
    sl.value.postln;
    ~model[\freq] = ~spec1.map(sl.value)
});
~view2 = NumberBox(~win,Rect(0,200,200,200)).action_({ |nb|
    ~model[\amp] = ~spec2.map(nb.value)
});

~win.onClose_{ ~controller.remove }

);

~model[\freq] = 234; ~model.changed(\freq);
~model[\amp] = -14.5; ~model.changed(\amp);
~model


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

// make a routing matrix!!
(
var win = Window().front;
var numStrips = 4;
var numPages = 6;
var inSources = Array.fill(8,{ |i| ["i", i] });
var outSources = Array.fill(4,{ |i| ["o", i] });
var stripSources = numPages.collect({ |page| numStrips.collect({ |strip| [page, strip] }) }).flatten(1);
var allSources = inSources ++ stripSources ++ outSources;
var sendButtonFunc = { |color|
    var gainView = View().layout_( 
        VLayout(
            Slider().maxWidth_(30),
            NumberBox().maxWidth_(30),
            Button().maxWidth_(30)
        ).margins_(2)
    );
    Button()
    .maxWidth_(30)
    .states_([["⬇︎",color,Color.clear],["⬇︎",Color.white,color]])
    .mouseDownAction_({ |view, x, y, modifiers, buttonNumber, clickCount|
        if(buttonNumber == 1,{
            Menu(
                CustomViewAction(gainView)
            ).front
        })
    })
};

var buttons = allSources.collect({ |src, index|
    Button()
    .maxWidth_(30)
    .maxHeight_(20)
    .states_([["%:%".format(*src), Color.black, Color.white],["%:%".format(*src), Color.white, Color.black]]) 
    .action_({ |thisBut|
        buttons.do(_.value_(0));
        thisBut.value_(1);
        stripStack.index_(index)

    })
});

var inFaders = inSources.collect({
    View().layout_(
        HLayout(
            *allSources.collect({ |src, index|
                var col = Color.blue(index / allSources.size);
                if(index < inSources.size,{ View().maxWidth_(30).maxHeight_(20).background_(col) },{ sendButtonFunc.value(col) })
            })
        ).margins_(0)
    )
});

var stripFaders = stripSources.collect({ |strSrc, strIndex|
    View().layout_(
        HLayout(
            *allSources.collect({ |srcArray, srcIndex|
                var col = Color.blue(srcIndex / allSources.size);
                var pageIndex = srcArray[0]; 
                var stripIndex = srcArray[1]; // 0-3
                var stripNum = (srcIndex - inSources.size).max(0); // 0-stripSources.size

                case
                { pageIndex == "o" }{ sendButtonFunc.value(col) }
                { stripNum > strIndex and: { strIndex % numStrips != stripIndex }}{ sendButtonFunc.value(col) }
                { View().maxWidth_(30).maxHeight_(20).background_(col) }
            })
        ).margins_(0)
    )
});

var outFaders = outSources.collect({
    View().layout_( 
        HLayout( *allSources.collect({ |src, index| 
            var col = Color.blue(index / allSources.size);
            View().maxWidth_(30).maxHeight_(20).background_( col ) 
        }) ).margins_(0)
    )
});


var stripStack = StackLayout( *(inFaders ++ stripFaders ++ outFaders) );
buttons[0].valueAction_(1);

win.layout_(
            VLayout(
                HLayout( *buttons ),
                stripStack,
                HLayout(
                    *allSources.collect({ |src| StaticText().maxWidth_(30).maxHeight_(20).align_(\center).string_("%-%".format(*src)) })
                )
            )
);
)
~stack.index_(1)
~s2.index_(2)
ScopeView
FreqScope
Stethoscope
23-5

(
w = Window().background_(Color.black(0.4)).front;

w.layout_(
    VLayout(
        *36.collect({ |i| NS_LevelMeter(i).value_(1.0.rand) })
    )
);
w.layout.spacing_(NS_Style.viewSpacing).margins_(NS_Style.viewMargins)
);

f = Font("Helvetica",12)
f.size
f = Pseq(Font.availableFonts).asStream
Font.defaultFont
Font.availableFonts.dopostln

"trlpet".bounds(Font("Helvetica",12));
"trlpet".bounds(Font("Mukta",12))
"trlpet".bounds(Font("Nadeem",12))

ZeroXBufRd
ZeroXBufWr
Dunique


(
SynthDef(\grainFM,{
    var numChans = 2;
    var tFreq = \tFreq.kr(4!3);
    var trig = Impulse.ar(tFreq);
    var grainDur = \grainDur.kr(1);
    var carHz = \carHz.kr(40!3);
    var modHz = \modHz.kr(2!3);
    var index = \index.kr(1);
    var pan = Demand.ar(trig, 0, Dwhite(-0.5,0.5));
    var env = -1;
    var sig = 3.collect{ |i|
        GrainFM.ar(numChans, trig[i], grainDur, carHz[i], modHz[i], index, pan[i], env);
    };

    sig = sig.sum;

    sig = LeakDC.ar(sig);

    sig = sig * \amp.kr(0.1);
    sig = sig * Env.asr().ar(\gate.kr(1));

    Out.ar(0,sig)
}).add
)

a = Synth(\grainFM)
(
b = {80.rrand(120)}!3;
a.set(\tFreq,[12,13,14],\grainDur, 0.2,\carHz,b, \modHz, b * 2, \index,2, \amp, 0.2 )
)
a.set(\amp,0.2,)
