NS_Style {
    classvar palette, <styles;

    *initClass {
        palette = (
            white:  Color.fromHexString("#ffffff"),
            aqua:   Color.fromHexString("#34d9d3"),
            green:  Color.fromHexString("#53e128"),
            yellow: Color.fromHexString("#fbd31a"),
            orange: Color.fromHexString("#fba011"),
            pink:   Color.fromHexString("#fb6ab6"),
            red:    Color.fromHexString("#e73311"),
            purple: Color.fromHexString("#b132d4"),
            blue:   Color.fromHexString("#4a49aa"),
            black:  Color.fromHexString("#252525"),
        );

        // this can surely be better, maybe Events are not the right solution
        // without copy, the keys overwrite the alpha channel, for example :|
        styles = (
            parent: palette,

            transparent:  Color.clear,
            listening:    palette['pink'].copy,
            assigned:     palette['blue'].copy,

            mainColor:    palette['pink'].copy,

            bGroundDark:  palette['black'].copy,
            bGroundLight: palette['white'].copy,
            textDark:     palette['black'].copy,
            textLight:    palette['white'].copy,
            darklight:    palette['black'].copy.alpha_(0.8),
            highlight:    palette['white'].copy.alpha_(0.4),

            // symbols
            play:  "▶", // a leading space centers the icon better in Helvetica
            pause: "⏸︎",
            stop:  "⏹",
            mute:  "M",
            show:  "s",
            clear: "x",

            // fonts
            // consider: Sathu,
            smallFont:   ["Helvetica", 10],
            defaultFont: ["Helvetica", 12],
            bigFont:     ["Helvetica", 14],

            // margins: space (l, t, r, b) between parent window and children
            // spacing: space between children

            windowMargins: [4, 4, 4, 4],
            windowSpacing: 0,

            viewMargins: [4, 4, 4, 4],
            viewSpacing: 4,

            innerMargins: [0, 0, 0, 0],
            innerSpacing: 2,

            border:   1.5,
            radius:   8, 
        )//.parent_(palette);
    }

    *new { |style|
        ^styles.atFail(style.asSymbol,{ "style: % not found".format(style).warn })
    }
}




/*
## probably worth checking this out at some point...
## but it may not be relevant as I'm drawing all my own widgets

var palette = QPalette()
.setColor(styles.windowBG,  'window')        // window BG, StaticText BG, (some) View borders?
.setColor(styles.textLight, 'windowText')    // Static Text on a Window
.setColor(styles.buttonBG,  'button')        // button,fader, knob BG
.setColor(styles.textLight, 'buttonText')    // button text, fader/knob pips
.setColor(Color.magenta,    'brightText')
.setColor(styles.viewBG,    'base')          // backgound of TextField
.setColor(styles.textDark,  'baseText')      // entries in ListView, text in Drag..
.setColor(Color.red,  'alternateBase')     // backgound in ListView? Or maybe nothing...
.setColor(styles.highlight, 'highlight')     // ListView highlight, focus borders on Drag... and TextField 
.setColor(styles.textDark, 'highlightText')  // ListView selected text

these are all used to create borders on random Views, very inconsistent
.setColor(Color.clear, 'light') 
.setColor(Color.clear, 'midlight')
.setColor(Color.clear, 'middark') 
.setColor(Color.clear, 'dark') 
.setColor(Color.clear, 'shadow'); 

.setColor(Color.rand, 'button', 'disabled');
QtGUI.palette = palette
*/
