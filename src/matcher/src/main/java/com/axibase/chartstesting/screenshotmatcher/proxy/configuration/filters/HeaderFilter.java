package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;

/**
 * Created by aleksandr on 25.10.16.
 */
public class HeaderFilter implements LineFilter {
    @Override
    public String filter(String line, Portal portal) {
        String header = "timezone = UTC\n" +
            "colors = palegoldenrod, darkblue, lightblue, pink, paleturquoise" +
            "darkseagreen, powderblue, deepskyblue, azure, lightslategray" +
            "lightgray, royalblue, mediumvioletred, darkmagenta, maroon" +
            "goldenrod, mediumturquoise, lightyellow, yellow, lemonchiffon" +
            "blueviolet, deeppink, lavenderblush, red, tan, purple, darkkhaki" +
            "springgreen, moccasin, dodgerblue, mintcream, aquamarine, salmon" +
            "beige, papayawhip, grey, lightsalmon, saddlebrown, lightsteelblue" +
            "silver, chocolate, firebrick, indigo, indianred, darkolivegreen" +
            "darkorange, lightgreen, slateblue, darkgreen, olivedrab, slategrey" +
            "navajowhite, brown, dimgray, violet, skyblue, cadetblue, oldlace" +
            "orchid, lightgoldenrodyellow, snow, floralwhite, wheat, sandybrown" +
            "teal, thistle, blanchedalmond, ghostwhite, hotpink, cornsilk" +
            "yellowgreen, cornflowerblue, mistyrose, gold, olive, forestgreen" +
            "orangered, slategray, tomato, mediumaquamarine, lightslategrey" +
            "cyan, mediumspringgreen, darkgrey, darkorchid, linen, turquoise" +
            "seashell, rosybrown, peachpuff, burlywood, mediumslateblue" +
            "crimson, midnightblue, honeydew, darkslateblue, darkturquoise" +
            "whitesmoke, lavender, lightgrey, magenta, bisque, lime" +
            "greenyellow, mediumblue, lightcoral, coral, mediumpurple" +
            "rebeccapurple, limegreen, green, sienna, lawngreen" +
            "chartreuse, darkviolet, navy, darkslategrey, aqua" +
            "mediumseagreen, darkgray, plum, black, seagreen" +
            "lightseagreen, antiquewhite, orange, lightskyblue" +
            "peru, dimgrey, lightcyan, darkslategray, steelblue" +
            "palegreen, darkcyan, lightpink, ivory, darkgoldenrod" +
            "fuchsia, mediumorchid, gray, palevioletred, darkred" +
            "white, blue, aliceblue, darksalmon, khaki, gainsboro";

        if (portal.hasEndtime()) {
            header = "endtime = "+portal.getEndtime()+"\n" + header;
        }

        return new ReplaceMatchingFilter("\\[configuration\\]", "[configuration]\n" + header).filter(line, portal);
    }
}
