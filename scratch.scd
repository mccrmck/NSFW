
NS_ServerHub.servers['nsfw_0'].server.options.numOutputBusChannels
NS_ServerHub.servers['nsfw_0'].strips[0][2].inSynth.get(\thru,{|g| g.postln})
NS_ServerHub.servers['nsfw_0'].strips[0][0].inSynthGate
NS_ServerHub.servers['nsfw_0'].strips[0][0].setInSynthGate(0)
NS_ServerHub.servers['nsfw_0'].strips[1][3].inSink.module.synths
NS_ServerHub.servers['nsfw_0'].strips[0][0].moduleArray[0].controls[3].object
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



NS_Transceiver.discreteQueue.dopostln
NS_Transceiver.continuousQueue.dopostln

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
// MultiChannelTest has a <|0|> number box thing, this should be abstracted, no?
// the above will also feature in OutChannelStrip! :)

// gotta go through NS_Transceiver again, and NS_Module().save needs a look over + TESTING
// what about buttons with one state? Like triggers, etc.
// ang. above, must go through all modules that have one-state buttons (triggers) and test with osc messages sending 1 and 0 (should round, no?)
// should always boot w/ 8 possible inputs, but I can choose inBusses manually
// entire ServerHub/inputmodule needs a rework, also the graphic eq





n = NS_MetaRegressor(Group())
n.controls[4].actionDict.dopostln
n.controls[4].actionDict['mlpMeter'].notNil
n.toggleVisible
n.free


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

SCViewHolder

CmdPeriod


// rethink Benjolin osc interface
// OSC_XY     args -> snap(true),                width, height
// OSC_Fader  args -> snap(true), horizontal(t), width, height
// OSC_Switch args -> numpads(3), columns, mode,  width, height // slide, tap
// OSC_Button args ->                      mode,  width, height // toggle, push

// OSC_Knob   args -> snap(mode),                width, height // vertical, snap
// must edit OSC_... code so that it doesn't send /touch messages unless event.type == start or stop
// must edit MultiFader so that it works...and so that it sends separate OSC addresses

p = OpenStageControl.boot
p.pid.pidRunning
p.kill
p.makeWindow // catch errors when o-s-c isnot running
p.closeWindow
p.addModuleFragment(0,0,0,NS_BPFDelay)
p.removeModuleFragment(0,0,0)


/*
can I find a better way to arrange widgets inside OSC_Panel?
must make better use of the width/height args
maybe the widget array can be a multidimensional array, each inner array 
is a row or column? I can technically already do this with panels..
okay, so the real issue is complex modules(many widgets) creating big strings,
and also I can't get the html to show up on top, which should be fixed..
*/
