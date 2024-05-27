NS_Module {
    var <>modGroup, <>bus;
    var <>synths, <>controls, <>assignButtons, <inBus;
    var paused = false;
    var <>win, <layout;

  *new { |group, bus|
      ^super.newCopyArgs(group, bus).init
  }

  initModuleArrays { |numSlots|
      synths = List.newClear(0);
      controls = List.newClear(0);
      assignButtons = List.newClear(numSlots);
  }

  makeWindow { |name, bounds|
      var cols = [Color.rand, Color.rand];
      var start, stop;
      win = Window(name,bounds,true);
      start = [win.view.bounds.leftTop,win.view.bounds.rightTop].choose;
      stop  = [win.view.bounds.leftBottom,win.view.bounds.rightBottom].choose;

      win.drawFunc = {
          Pen.addRect(win.view.bounds);
          Pen.fillAxialGradient(start, stop, cols[0], cols[1] );
      };

      win.alwaysOnTop_(true);
      win.userCanClose_(false);
      win.front
  }

  free {
      win.close;
      synths.do({ |synth| synth.set(\gate,0) });
  }

  pause {
      synths.do({ |synth| synth.set(\pauseGate, 0) });
      modGroup.run(false);
      paused = true;
  }

  unpause {
      synths.do({ |synth| synth.set(\pauseGate, 1); synth.run(true) });
      modGroup.run(true);
      paused = false;
  }

  show {
      win.visible = true;
      win.front;
  }

  hide {
      win.visible = false;
  }

  toggleVisible {
      var bool = win.visible.not;
      win.visible = bool;
      if( bool,{ win.front })
  }

}
