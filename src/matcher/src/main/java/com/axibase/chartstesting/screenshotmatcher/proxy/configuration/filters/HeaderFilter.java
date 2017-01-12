package com.axibase.chartstesting.screenshotmatcher.proxy.configuration.filters;

import com.axibase.chartstesting.screenshotmatcher.core.Portal;

/**
 * Created by aleksandr on 25.10.16.
 */
public class HeaderFilter implements LineFilter {
    @Override
    public String filter(String line, Portal portal) {
        String header = "timezone = UTC\n" +
            "colors = blueviolet, aquamarine, burlywood, chartreuse, crimson, " + 
            "darkblue, darkgoldenrod, darkorange, darkslateblue, darkcyan, " + 
            "deeppink, dodgerblue, greenyellow, green, lightgreen, lightsalmon, " + 
            "mediumaquamarine, midnightblue, olivedrab, red, saddlebrown, " +
            "skyblue, slategrey, tomato, yellow, yellowgreen, steelblue, khaki, " + 
            "coral, brown, palegoldenrod, lightblue, pink, paleturquoise" +
            "darkseagreen, powderblue, deepskyblue, azure, lightslategray" +
            "lightgray, royalblue, mediumvioletred, darkmagenta, maroon" +
            "goldenrod, mediumturquoise, lightyellow, lemonchiffon" +
            "lavenderblush, tan, purple, darkkhaki, springgreen, moccasin, " + 
            "mintcream, salmon, beige, papayawhip, grey, lightsteelblue" +
            "silver, chocolate, firebrick, indigo, indianred, darkolivegreen" +
            "slateblue, darkgreen, navajowhite, dimgray, violet, cadetblue, oldlace" +
            "orchid, lightgoldenrodyellow, snow, floralwhite, wheat, sandybrown" +
            "teal, thistle, blanchedalmond, ghostwhite, hotpink, cornsilk" +
            "cornflowerblue, mistyrose, gold, olive, forestgreen" +
            "orangered, slategray, lightslategrey" +
            "cyan, mediumspringgreen, darkgrey, darkorchid, linen, turquoise" +
            "seashell, rosybrown, peachpuff, mediumslateblue" +
            "honeydew, darkturquoise" +
            "whitesmoke, lavender, lightgrey, magenta, bisque, lime" +
            "mediumblue, lightcoral, mediumpurple" +
            "rebeccapurple, limegreen, sienna, lawngreen" +
            "darkviolet, navy, darkslategrey, aqua" +
            "mediumseagreen, darkgray, plum, black, seagreen" +
            "lightseagreen, antiquewhite, orange, lightskyblue" +
            "peru, dimgrey, lightcyan, darkslategray" +
            "palegreen, lightpink, ivory" +
            "fuchsia, mediumorchid, gray, palevioletred, darkred" +
            "white, blue, aliceblue, darksalmon, gainsboro";

        if (portal.hasEndtime()) {
            header = "endtime = "+portal.getEndtime()+"\n" + header;
        }

        return new ReplaceMatchingFilter("\\[configuration\\]", "[configuration]\n" + header).filter(line, portal);
    }
}
