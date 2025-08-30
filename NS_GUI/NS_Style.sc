NS_Style {
    classvar styles;

    *initClass {

        styles = (
            // colors
            transparent:  Color.clear,
            listening:    Color.fromHexString("#ff0088"),
            assigned:     Color.fromHexString("#0091ff"),// b827e8

            bGroundDark:  Color.fromHexString("#232325"),
            bGroundLight: Color.fromHexString("#fdfeff"),
            textDark:     Color.fromHexString("#101012"),
            textLight:    Color.white,
            darklight:    Color.gray(0.4).alpha_(0.8),
            highlight:    Color.white.alpha_(0.4),

            yellow:       Color.fromHexString("#ffd50a"),
            orange:       Color.fromHexString("#ff6f00"),
            red:          Color.fromHexString("#e3030f"),
            green:        Color.fromHexString("#1c911c"),

            // symbols
            play: "▶",
            pause: "⏸︎",
            stop: "⏹",
            mute: "M",
            show: "S",
            clear: "ⅹ",

            // fonts
            defaultFont: ["Helvetica", 12],
            smallFont:   ["Helvetica", 10],
            bigFont:     ["Helvetica", 14],

            // margins: space (l, t, r, b) between parent window and children
            // spacing: space between children

            // serverWindow
            windowMargins: [4, 4, 4, 4],
            windowSpacing: 2,

            // moduleSinks, assignButton
            viewMargins: [4, 4, 4, 4],
            viewSpacing: 2,

            // modules
            modMargins: [4, 4, 4, 4],
            modSpacing:  2,

            inset:    2,
            radius:   4, 
        );

        //var palette = QPalette()
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
       // .setColor(Color.clear, 'light') 
       // .setColor(Color.clear, 'midlight')
       // .setColor(Color.clear, 'middark') 
       // .setColor(Color.clear, 'dark') 
       // .setColor(Color.clear, 'shadow'); 

       //.setColor(Color.rand, 'button', 'disabled');
        //QtGUI.palette = palette
    }

    *new { |style|
        ^styles.atFail(style.asSymbol,{ "style: % not found".format(style).warn })
    }
}
