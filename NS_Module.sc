NS_Module {
    var <>modGroup;
    var <>synths, <>controls, <>assignButtons, <inBus;
    var paused = false;
    var <>win, <layout;

  *new { |group|
      ^super.newCopyArgs(group).init
  }

  initArrays { |numSlots|
      controls = List.newClear(0);
      assignButtons = List.newClear(numSlots);
      synths = List.newClear(numSlots);
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

      win.front
  }

  pause {
      synths.do({ |synth| synth.set(\pauseGate, 0) });
      modGroup.run(false);
      paused = true;
  }

  unpause {
      synths.do({ |synth| synth.set(\pauseGate, 1); synth.run(true);});
      modGroup.run(true);
      paused = false;
  }

}
