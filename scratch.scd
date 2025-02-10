
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


NS_ServerHub.boot;


n = NS_MLPMeter()


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
// find a better way to set the sizes of NS_ControlFader elements (text, slider, numBox)


// TO FIX:
// ControlSwitch, ControlMenu, ControlButton -> integers and floats as values...should probably be ints?
// MonoSumSend, SumSplay use NS_ControlMenu, but I think we need a better UI for choosing output Channels 
//  SamplePB, has DragSinks which get saved!!
// DelAmpComp -> is a mess, needs a better name too!
// MultiChannelTest has a <|0|> number box thing, this should be abstracted, no?
// the above will also feature in OutChannelStrip! :)
// must find a standard for width/height in module windows so that faders look uniform
// ControlFader method: .setWidths(20,120,60) (or something?) for all three elements 
// BUG: because the .init funcs for NS_Control update the value, if the default bypass value is 0,
// it calls strip.inSynthGate_(0), which decrements the insynthGate value...maybe this is fixed an update == false

// gotta go through NS_Transceiver again, and NS_Module().save needs a look over + TESTING
// module.save() needs to consider buffer paths saved in 
// fix buttons so I can send colors as well as labels
// what about buttons with one state? Like triggers, etc.
// ang. above, must go through all modules that have one-state buttons (triggers) and test with osc messages sending 1 and 0 (should round, no?)
// fader can set label to nil?
// clean up the modulelist code...better variable names, comments maybe?
// maybe clear all the layout spacing/margins and start over? make them a NSFWStyle variable?
// chorus sucks I think, must fix!!
// should always boot w/ 8 possible inputs, but I can choose inBusses manually
// entire ServerHub/inputmodule needs a rework, also the graphic eq
// can style ListView(ModuleList) w/ .font_()
// figure out amp/mix for SamplePB -> do we want a wet/dry mix (like SwellFB?)
// consider samplePB (and bufferPB?): do I need instance variable bufferPath?


c = NS_Control(\test,\db)
m = NS_MLPMeter(c)
a = m.control !? { m.control.normValue } ?? 0


Pitch

nil !? 4 ?? { "test".postln } 

// crazy ideas:
// add a popDown window at the bottom edge of the ServerWindow with a ServerMeterView, maye with a dark skin?

g = Group()
n = NS_SamplePB(Group())
n.free


n = NS_Control(\test,ControlSpec(0,4,\lin,1))
n.value_(0)
a = NS_ControlMenu(n,(0..4))
a.view.front



n = NS_ModuleList()
n.asView.front
n.asView.children[0].font_(Font("Menlo",20))


r = NS_Tanh()
r.free
o = NS_Vocoder()
p.free
o.free
o.toggleVisible

s.avgCPU
s.peakCPU




n = NS_MetaRegressor(Group())
n.controls[4].actionDict.dopostln
n.controls[4].actionDict['mlpMeter'].notNil
n.toggleVisible
n.free

a = Button().states_(["test"]).action_({|b| b.value.postln}).front

a = NS_Control(\test,ControlSpec(0,0,\lin,1))
a.addAction(\post,{ |c| 4.postln; c.value.postln })
a.value_(0)
a.value

1.0.asBoolean


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


// screenshot, could be nice for an icon
Image.fromWindow(view).setSize(w,h,resizeMode) // [ doNotScale, ignoreAspectRatio, keepAspectRatio, keepAspectRatioByExpanding ]


OSCFunc.trace(false)
