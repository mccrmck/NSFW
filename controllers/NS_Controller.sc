NS_Controller {

    *initClass {
        ShutDown.add({ this.cleanupAll });
        //CmdPeriod.add({ this.cleanupAll }); // or?
    }

    *allActive {
        ^this.subclasses.select({ |ctrl| ctrl.connected == true });
    }

    *cleanupAll {
        this.allActive.do(_.cleanup)
    }
    
    // controllers add themselves to active upon init/connect
    *connect { this.subclassResponsibility(thisMethod) }
    
    // free resources, close windows, etc.
    *cleanup { this.subclassResponsibility(thisMethod) }
   
    // create view for serverHub interface
    *drawView { this.subclassResponsibility(thisMethod) }
   
    // switch matrixServer ChannelStrip page
    *switchStripPage { this.subclassResponsibility(thisMethod) }

    *save { this.subclassResponsibility(thisMethod) }

    *load { this.subclassResponsibility(thisMethod) }
}
