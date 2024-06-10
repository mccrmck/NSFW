NS_SwapGrid : NS_ControlModule {
    var <view;
    var <buttons;
    var <currentActiveStrips;

    *new { |nsServer|
        ^super.new.init(nsServer)
    }

    init { |nsServer|

        this.initControlArrays(4);

        currentActiveStrips = [0,0,0,0];

        buttons = 4.collect({ |column|
            controls.add(
                NS_Switch(nil,""!6,{ |switch|
                    var pageIndex = switch.value.indexOf(1);
                    var oldStrip = currentActiveStrips[column];

                  // nsServer[oldStrip][column].pause;
                  // nsServer[pageIndex][column].unPause;
                  currentActiveStrips[column] = pageIndex;

                  // update controllers

                  // change colour
                  defer {
                      nsServer.strips[oldStrip][column].asView.background_ ( Color.clear );
                      nsServer.strips[pageIndex][column].asView.background_( Color.fromHexString("#fcb314") );
                  }

              }).maxWidth_(90);
          )
      });

      view = View().layout_(
          HLayout(
              *4.collect({ |columnIndex|
                  VLayout(
                      controls[columnIndex],
                      NS_AssignButton().maxWidth_(45).setAction(this, columnIndex, \switch)
                  )
              })
          )
      );

      4.do({ |i| controls[i].valueAction_(0) });
      controls.do({ |switch| switch.layout.spacing_(2).margins_(2) });
      view.layout.spacing_(2).margins_([2,0]);
  }

  asView { ^view }
}
