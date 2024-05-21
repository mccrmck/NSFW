



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

ControlSpec

(
var bounds = Window.availableBounds;
var win = Window("NSFW",bounds.width_(bounds.width/2));

win.layout_(
    HLayout(
        ~test = NS_QTXY("freq",\freq,"rq", \amp,{ |f|  },),
    )
);

win.front;
)

Warp

~test.y
\rq.asSpec
a = NS_RingMod().makeWindow("ringer")
a.controls[1].valueAction_(0.5)

Button

OSCFunc.trace(false)
NetAddr.localAddr
   
Server

DragSource
String

b = NS_Panel("50%","20%",true,[NS_Fader(),NS_Fader("10%")])

b.id
b.widgetArray
b.widgetArray[1] = NS_MultiFader()

l = NS_Fragment(false,[
    NS_Fader(nil,"10%"),
    NS_Button(nil,nil,'p'),
    NS_Panel(widgetArray:[
        NS_Switch(nil,nil,true,'slide',8),
        NS_XY(snap:true)
    ])
])
l.write("test1")



"\"widgets\": [%,%]".format(NS_Fader().oscString)


h = [NS_Fader(snap:true),NS_Fader("30%",nil,false)].collect({ |widget|
    widget.oscString;
});

h[1]

[NS_Fader(),NS_Fader("10%")].oscString

"\"widgets\": [%]".format("\%,\%").format(*h)

b = 4.collect({ "\%"}).

"%".format("%".ccatList("%"!(4)).replace.format(*h))


a = NS_MultiFader("20%","50%",true,true,10)
a.oscString


"\\\"".postln


"\"for (var i in value) {\\n    value[i] = Math.max(0, Math.min(1, value[i]))\\n}\\n\\nset(\"this\", value, {sync: false, send: false})\"".postln
