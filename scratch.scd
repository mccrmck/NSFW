



Slider




x.action_({|val|
    x.value.postln;

})




w = Window("test",).front
x = Slider()
z = Slider()
g = HLayoutView(w).visible_(true).background_(Color(1))
Slider(g).resize_(5); TextField(g).resize_(5);Slider2D(g).resize_(5);
w.layout = HLayout(g,x,z)
w.layout.spacing_(0).margins_(0)
g.setProperty(\spacing,0).setProperty(\margins,0)



x = Slider().front
x.mouseDownAction_({|...args| args.postln;})

.pause

SimpleController


a = [0,4]
b = Slider().front
a.addDependant(b)
a.dependants

Object.dependantsDictionary.dopostln

Spec
s.meter
ControlSpec

(
var bounds = Window.availableBounds;
var win = Window("NSFW",bounds.width_(bounds.width/2));

win.layout_(
    HLayout(
       ~test = NS_Fader(win,"test",\freq,{ |f|  },).maxWidth_(40),
    )
);
win.front;
)

~test.maxWidth_(500).maxHeight_(50)

45 * 8

~test
a = NS_RingMod()
b = NS_MainWindow()
(
var rand = 6.rand;
rand.postln;
b.pages.do({ |p| p.view.background_(Color.clear) });
b.pages[rand].view.background_(Color.yellow)
)


DragBoth().sizeHint

a.controls[0].value_([0.5,0.5])
a.controls[1].spec.map(0.25)

c = ControlSpec(-12,12,\lin)

c.constrain(24)
c.asSpec.map(0.25)

View().parent

a.layout.front
GridLayout
a = []
a = a.add(nil)



\db.asSpec.unmap(0.4)

OSCFunc.trace(false)
NetAddr.localAddr

4.ampdb

View().bounds.width
NS_StripView().win.front
