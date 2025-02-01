
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


n = NS_MLPMeter()

r = NS_MetaRegressor();
r = NS_RingMod();
b = NS_Benjolin();
b.free
r.free
Window.closeAll
r.win.bounds
r.controls[0].specs.size
.value_(40)


n = NS_Control('freq', \freq.asSpec)
n.addAction(\osc,{ |c| NetAddr("localhost",8080).sendMsg("/fader_1", c.normValue); "dict".postln })
n.addAction(\qtGui,{ |c| { f.value_(c.normValue) }.defer; "qt".postln })

n.actionDict.keys
n.removeAction('test')
n.spec_(\freq.asSpec)
n.value
n.value_(100)

f = Slider().action_({ |f| n.normValue_(f.value,\qtGui); "slider".postln }).front
o = OSCdef(\osc,{ |msg| n.normValue_( msg[1],\osc ); "osc".postln },'/fader_1')
o.free

NetAddr("localhost",8080).sendMsg("/button_30", 0.0)

t.controls[2].normValue_(0.0)

OSCFunc.trace(false)
a = ["button", "switch", "touch"].collect({ |string| "this is a ".contains(string) })
a.asInteger.sum
[true, true, false].asInteger

Boolean

b = List.newClear(0)
b.clear
b
b.add(1)
b.add(5)
a = b.removeAt(0); b
a
"button, switch".contains('button'.asString)

NS_Transceiver.discreteQueue.dopostln
NS_Transceiver.continuousQueue.dopostln

x = 1
x.do{|i| i.postln }

NS_Control.dumpInterface
Window.closeAll



a = NetAddr("localhost",8080)
a.sendMsg("/a_1",1); a.sendMsg("/a_2",0)


a = List[0,1,2]
a.size
a.removeAt(0); a
b = a.collect({ 4.rand })
b.class

t = NS_Test2()
t = NS_RingMod2()
t.free

g = t.save
g[0].class
g.dopostln
t.oscFuncs.dopostln
t.controls[2].addAction('poll',{ |c| c.value.postln})
t.controls[4].actionDict['controller'].value
t.controls.do(_.actionDict)
t.free



OSCFunc.trace()

NS_Transceiver.clearQueues
NS_Transceiver.continuousQueue.dopostln
NS_Transceiver.discreteQueue.postln
t.free
t.controls[0].actionDict.keys
t.controls[1].actionDict.keys
t.controls[2].normValue




r.class.superclasses.includes(NS_ControlModule)

t = NS_Test2()
t.free
t = NS_Test()
a = NetAddr("localhost",8080)
a.sendMsg("/fader_32",0)
t.controls[1].addAction(\osc,{|c| a.sendMsg("/fader_32",c.normValue)  })

OSCFunc.trace(false)
t.controls[1].value_(0.4)
t.controls[2].value_(0)
t.win.close
t.free
s.plotTree


// NS_GUI common methods
// getters:
// value, spec, round, label, 
// setters:
// value, round, 

r = NS_RingMod();
r.free
n.value
n.spec.unmap(350)
a = n.value
n.value
n.view.visible_(true)

[0.5555,0.4343].round(0.01)

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



x = x ?? { 4 }  
b
x

t.actions[0].background
y.setInnerExtent(800,400)
y.bounds_(150,50,240,400)

y.view.alpha_(1)
Window.closeAll

v = Button(Window().front.acceptsMouseOver_(true)).mouseOverAction_({ |view, x, y, mod, butNum, count| [view, x, y, mod, butNum, count].postln; });

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


0.max(30)

[1,1,0,0].convertDigits(2) //12
[0,1,1,0].convertDigits(2) //6
[0,0,1,1].convertDigits(2) //3
[1,0,0,1].convertDigits(2) //9

r = Rect()
r.right_(600)
r

[true,false].asInteger

true and: true

true.not
w.close
w.resizeTo(200,500)
w.background_(Color.clear)
w.win.alpha_(1)
w.uView.alpha_(0.8)
w.uView.layout.children
w.uView.layout_(VLayout(Button(),Button()))

50.atan    
w = NS_Window()
(
w.layout_(
    HLayout(
        Slider().background_(Color.red.alpha_(0.1)),
        Slider(),
        
    )
)
)
w.refresh
w.close
w.win.view.class.currentDrag



(
w = Window(border: true).front;
w.view.acceptsMouseOver_(true);

w.layout_(
    VLayout(
        Button().mouseMoveAction_({ 0.postln})
    )
)
)


MouseX

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

