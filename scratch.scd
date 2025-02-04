
NS_ServerHub.servers['nsfw_0'].server.options.numOutputBusChannels
NS_ServerHub.servers['nsfw_0'].strips[0][2].inSynth.get(\thru,{|g| g.postln})
NS_ServerHub.servers['nsfw_0'].strips[0][0].inSynthGate
NS_ServerHub.servers['nsfw_0'].strips[0][0].setInSynthGate(0)
NS_ServerHub.servers['nsfw_0'].strips[1][3].inSink.module.synths
NS_ServerHub.servers['nsfw_0'].strips[0][0].moduleArray[0].controls[3].object
NS_ServerHub.servers['nsfw_0'].strips[1][0].inSynthGate
NS_ServerHub.servers['nsfw_0'].strips.deepDo(2,{ |strp,i| strp.inSynthGate.postln; if(i % 4 == 3,{" ".postln}) })
NS_ServerHub.servers['nsfw_0'].window.swapGrid.controls.do({ |c| c.value.postln })
NS_ServerHub.servers['nsfw_0'].window.swapGrid.controls[0].value_(1)
NS_ServerHub.servers['nsfw_0'].server.plotTree;
NS_ServerHub.servers['nsfw_0'].server.scope;
NS_ServerHub.servers['nsfw_0'].server.freqscope;
NS_ServerHub.servers['nsfw_0'].server.meter;
NS_ServerHub.servers['nsfw_0'].server.peakCPU;
NS_ServerHub.servers['nsfw_0'].server.avgCPU;
NS_ServerHub.servers['nsfw_0'].server.numUGens;

n = NSFW(OpenStageControl,[64]);

c = ControlSpec(0,3,'lin',1.asInteger)
c.asSpec

c = NS_Control(\freq,\freq)
c.value_(1342.1)
n = NS_ControlKnob(c).view.front

NS_ServerHub.boot;

x = [0,1,2,3]
x[1]

n = NS_MLPMeter()

r = NS_MetaRegressor();
r = NS_RingMod();
b = NS_Benjolin();
b.free
r.free
Window.closeAll

NS_Transceiver.discreteQueue.dopostln
NS_Transceiver.continuousQueue.dopostln

NS_Control.dumpInterface
Window.closeAll;



// this is not accounted for yet:
// 1. assign button 1 == on, module is added to the queue
// 2. assign button 2 == on, module is added to the queue
// 3. assigm button 1 == off, module is NOT YET REMOVED FROM THE QUEUE
// some sort of descructor for GUI elements -> control.removeAction(\qtGui)
// find a better way to control the sizes of NS_ControlFader elements

// TO FIX:
// ControlSwitch -> integers and floats as values...should probably be ints?
// MonoSumSend, SumSplay had a popupmenu at controls[0], consider NS_ControlMenu?
// MultiIn has DragSinks which get saved!!
// DelAmpComp -> is a mess, needs a better name too!
// MultiChannelTest has a <|0|> number box thing, this should be abstracted, no?
// the above will also feature in OutChannelStrip! :)
// must find a standard for width/height in module windows so that faders look uniform
// ControlFader method: .setWidths(20,120,60) (or something?) for all three elements 

// gotta go through NS_Transceiver again, and NS_Module().save needs a look over + TESTING
// fix buttons so I can send colors as well as labels
// fader can set label to nil?
// put the folder list on ModuleList in a ListView also? Should the window float, or is there a better way to initiate the drag action?
// clean up the modulelist code...better varialbe names, comments maybe?
// maybe clear all the layout spacing/margins and start over? make them a NSFWStyle variable?
// should always boot w/ 8 possible inputs, but I can choose inBusses manually
// entire ServerHub/inputmodule needs a rework, also the graphic eq


n = NS_ModuleList()
n.asView.front

["one","two"].collect(_.asSymbol).indexOf('two')

c = ControlSpec(20,260,\freq)
c.map(0.5)
NS_ControlFader()




g[0].class
g.dopostln
t.oscFuncs.dopostln
t.controls[2].addAction('poll',{ |c| c.value.postln})
t.controls[4].actionDict['controller'].value
t.controls.do(_.actionDict)
t.free

c = NS_Control("w",ControlSpec(0,3,'lin'))

f = NS_ControlFader(c,'vert').max.view.front

c.value_(1)
c.normValue
(0..3).linlin(0,3,0,1)
c.actionDict
c.addAction(\osc,{ |a| NetAddr("localhost",8080).sendMsg('/switch_1', a.value );  })


NetAddr("localhost",8080).sendMsg('/switch_1',4)

o = OSCdef(\test,{ |msg|

   c.value_( msg[1], \controller);
   c.value.postln;

},'/switch_1');

g = Group()
n = NS_ChannelStrip(g)




/*
c = NS_Control(\freq,\freq.asSpec);
f = NS_Fader(c, 'horizontal');
d = NS_Control(\onOff,ControlSpec(0,1,'lin',1));
g = NS_Switch(d,'horizontal').addLabels((0..4); // .addLabels to make the initFunc consistent w/ NS_Fader?
// Do I need NS_Button? YES, to do the auto-.addAction/value update
// Do I need NS_Knob? maybe...NS_Gate
// try to make MLPMeter (or whatever you want to call it) consistent!
// Do I need any other kinds of GUI control elements?
*/


r.class.superclasses.includes(NS_ControlModule)

(
w = Window(bounds: Rect(250,250,30,20),border: true).front;
w.layout_(
    VLayout(
        n = NS_MLPMeter(),
        m = NS_MLPMeter(),
    )
);

w.layout.spacing_(4).margins_(4)

)
n.value = 0.8

a.value_(0)
a.view.maxHeight_(40)
2.wrap(0,1)

// add points
// train
// not predicting/predicting

FluidMLPRegressor



t.actions[0].background
y.setInnerExtent(800,400)
y.bounds_(150,50,240,400)

y.view.alpha_(1)
Window.closeAll


r = Rect()
b = Rect(40,30,580,20)
b.insetBy(10)
r + b
run
true xor: false

[true, false].asInteger

.insetBy
w = NS_Window()
w.view.bounds
w.bounds
g = Color.rand;
w.win.refresh
w.drawFunc_{
    var b = w.viewBounds;
    Pen.addRect(b);
    Pen.fillAxialGradient(b.leftTop, b.rightBottom, Color.black, g);
}
w.win.view.bounds
y = w.win.bounds.center
w.win.bounds_(Rect(0,0,200,200).center_(181@574))
w.win.bounds_(Rect(29,364 +80 ,400,400 - 80))
w.win.bounds_(0@0)
w.win.bounds_(Rect(00,50,400,400))
w.win.bounds_(w.win.bounds.moveBy(-50,0))
w.win.refresh
w.close

Window.closeAll



[1,1,0,0].convertDigits(2) //12
[0,1,1,0].convertDigits(2) //6
[0,0,1,1].convertDigits(2) //3
[1,0,0,1].convertDigits(2) //9


w = NS_Window()
(
w.view.layout_(
    HLayout(
        ScrollView(bounds: w.bounds).background_(Color.clear).layout_(
            HLayout(
               *{Slider()}!50,
                
            )
        )
    )
)
)
w.refresh
w.close
w.win.view.class.currentDrag





r = Rect(0,0,400,400)
a = Window.flipY(r)
a = Window.flipY(a)

w.layout
w.layout.margins_(20)
w.win.refresh

Window.closeAll

b = Button(y)
b.alpha


// run open-stage-control in a WebView!!
(
w = Window( ).setInnerExtent(650,650);

w.layout_(
    VLayout(
        WebView().url_("127.0.0.1:8080")
        )
);

w.layout.margins_(0).spacing_(0);

w.front
)
w.setInnerExtent(800,400)
w.bounds_(Rect(250,200,300,500))

Rect(250,200,300,500).moveBy(2,0)

OSCFunc.trace(true)

p = "node /Applications/open-stage-control.app/Contents/Resources/app/ --send 127.0.0.1:% --load '/Users/mikemccormick/Library/Application Support/SuperCollider/Extensions/NSFW/NS_Controllers/Open Stage Control/NSFW.json' ".format(NetAddr.localAddr.port).unixCmd

p.pidRunning
"kill %".format(p).unixCmd



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

Object

// screenshot, could be nice for an icon
Image.fromWindow(view).setSize(w,h,resizeMode) // [ doNotScale, ignoreAspectRatio, keepAspectRatio, keepAspectRatioByExpanding ]

