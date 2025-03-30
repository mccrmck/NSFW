NS_Style {

    *initClass {

        var styles = (
            // colors
            transparent:  Color.clear,
            assButt:      Color.fromHexString("#b827e8"),

            bGroundDark:  Color.fromHexString("#101012"),
            bGroundLight: Color.white,
            textDark:     Color.fromHexString("#101012"),
            textLight:    Color.white,
            darklight:    Color.gray(0.4).alpha_(0.8),
            highlight:    Color.white.alpha_(0.65),
            viewBG:       Color.white,
            windowBG:     Color.gray(0.2),

            orange:       Color.fromHexString("#ff6f00"),
            muteRed:      Color.fromHexString("#910c24"),
            playGreen:    Color.fromHexString("#1c911c"),

            // symbols
            play: "▶",
            stop: "⏹",

            // fonts
            defaultFont: ["Nadeem", 12],   // vertical spacing is weird
            smallFont:   ["Helvetica", 10],
            bigFont:     ["Helvetica", 14],

            /* 
            margins: space (l, t, r, b) between parent window and children
            spacing:space between children
            */

            // serverWindow
            windowMargins: [4, 4, 4, 4],
            windowSpacing: 2,

            // channelStrip, outChannelStrip, swapGrid, moduleList
            stripMargins: [2, 4, 2, 4], 
            stripSpacing: 2,

            // moduleSinks, assignButton
            viewMargins: [2, 0, 2, 0],
            viewSpacing: 0,

            // modules
            modMargins:  [4, 4, 4, 4],
            modSpacing:  2,

            radius:   4, 
            buttonW:  30,
            buttonH:  20,

        );

        var palette = QPalette()
        // .setColor(styles.windowBG,  'window')        // window BG, StaticText BG, (some) View borders?
        // .setColor(styles.textLight, 'windowText')    // Static Text on a Window
        // .setColor(styles.buttonBG,  'button')        // button,fader, knob BG
        // .setColor(styles.textLight, 'buttonText')    // button text, fader/knob pips
        // .setColor(Color.magenta,    'brightText')
        // .setColor(styles.viewBG,    'base')          // backgound of TextField
        // .setColor(styles.textDark,  'baseText')      // entries in ListView, text in Drag..
        // .setColor(Color.red,  'alternateBase')     // backgound in ListView? Or maybe nothing...
        // .setColor(styles.highlight, 'highlight')     // ListView highlight, focus borders on Drag... and TextField 
        // .setColor(styles.textDark, 'highlightText')  // ListView selected text

        // these are all used to create borders on random Views, very inconsistent
        .setColor(Color.clear, 'light') 
        .setColor(Color.clear, 'midlight')
        .setColor(Color.clear, 'middark') 
        .setColor(Color.clear, 'dark') 
        .setColor(Color.clear, 'shadow'); 

        styles.keysValuesDo({ |key, value|
            this.addUniqueMethod(key, { value })
        });

        //QtGUI.palette = palette
    }
}
