


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
var win = Window("NSFW",Rect(0,0,200,200));

win.layout_(
    VLayout(
       NS_Switch(win,(0..8),{|b| b.value.postln},'horz'),
       //~test = NS_Switch(win,(0..2),{|b| b.value.postln},\horz),
       NS_Switch(win,(0..8),{|b| b.value.postln},\horz),
       NS_Switch(win,(0..4),{|b| b.value.postln},\horz),
       ~test = NS_Fader(win,"test",\amp,{|b,i| [b,i].postln}),
    )
);
win.front;
)

"SinOsc".asSymbol.asClass.respondsTo('ar')
~test.slider

(
g = Group();
a = NS_RingMod()
)

a = NS_AmpMod()
a.synths[2].set(\modFreq,4)
a.controls[1].valueAction_( a.controls[1].spec.map(0.1) )
a.assignButtons
s.plotTree

m.free
s.meter
s.scope
s.freqscope
b = NS_MainWindow()


x = {[SinOsc.ar(500)]};
x.plot()
x.play

s.plotTree
c = NS_ChannelStrip(s,0,)
c.addInputModule(0)
r = NS_AmpMod(c.slots[0],c.stripBus)
r = NS_RingMod(c.slots[1],c.stripBus)
c.amp_(1)
c.toggleMute
s.meter

-3.dbamp
g = Group()
i = NS_Input(bus:0,numInChans:1) 
i.free
i.free
g.server

"test" === "test"
a.controls[0].value_([0.5,0.5])
a.controls[1].spec.map(0.25)

c = ControlSpec(-12,12,\lin)outBus

c = List.newClear(0)
c.isNil
c.do({ |i| i.set(\gate,0)})

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





(
    var view_a, view_b;
    
    view_a = View().layout_(VLayout(
        *30.collect {
            View()
                .fixedSize_(80@6)
                .background_(Color.rand)
        }
    ).spacing_(2));
    
    view_b = View().layout_(GridLayout.columns(
        *30.collect {
            View()
                .fixedSize_(40@20)
                .background_(Color.rand)
        }
        .clump(4)
    ).spacing_(2));
    
    3.do {
        |i|
        var top, h_layout, v_layout, swap_button;
        
        swap_button = Button()
            .states_([["horizontal"], ["vertical"]])
            .action_({
                |v|
                if (v.value == 0) {
                    h_layout.add(view_a, align:\center);
                    h_layout.add(view_b, align:\center);
                } {
                    v_layout.add(view_a, align:\center);
                    v_layout.add(view_b, align:\center);
                }
            });
        
        top = View(bounds:Rect(500 * i))
            .fixedWidth_(500)
            .layout_(VLayout(
                [ swap_button, stretch: 0 ],
                [
                    VLayout(
                        h_layout = HLayout(),
                        v_layout = VLayout()
                    ).spacing_(0).margins_(0),
                    stretch: 1
                ]
            )).front;
    };
)
