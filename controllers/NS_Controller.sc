NS_Controller {

    *initClass {
        ShutDown.add({ this.cleanUpAll });
        //CmdPeriod.add({ this.cleanupAll }); // or?
    }

    *allActive {
        ^this.subclasses.select({ |ctrl| ctrl.connected == true });
    }

    *cleanUpAll {
        this.allActive.do(_.cleanUp)
    }
    
    // controllers add themselves to active upon init/connect
    *connect { this.subclassResponsibility(thisMethod) }
    
    // free resources, close windows, etc.
    *cleanUp { this.subclassResponsibility(thisMethod) }
   
    // create view for serverHub interface
    *drawView { this.subclassResponsibility(thisMethod) }
   
    // switch server ChannelStrip page
    *switchStripPage { this.subclassResponsibility(thisMethod) }

    *save { this.subclassResponsibility(thisMethod) }

    *load { this.subclassResponsibility(thisMethod) }
}
