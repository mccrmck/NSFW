OSC_WidgetID {}

OSC_ButtonID : OSC_WidgetID {
    classvar <>id = 0;
    *initClass { id = 0; }
    *next { ^id = id + 1 }
    *setID { |newId| id = newId }
}

OSC_FaderID : OSC_WidgetID { 
    classvar <>id = 0;
    *initClass { id = 0; }
    *next { ^id = id + 1; } 
    *setID { |newId| id = newId }
}

OSC_PanelID : OSC_WidgetID {
    classvar <>id = 0;
    *initClass { id = 0; }
    *next { ^id = id + 1; } 
    *setID { |newId| id = newId }
}

OSC_XYID : OSC_WidgetID{
    classvar <>id = 0;
    *initClass { id = 0; }
    *next { ^id = id + 1; } 
    *setID { |newId| id = newId }
}
