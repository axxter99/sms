/**
 * JS library for the SMS User Tool Utilities.
 * @author lovemore.nalube@uct.ac.za
 **/

var smsUtils = (function($) {

    //Deal with an ajax error
    var error = function(errorMessage, _this, element) {
        var errorElement = $("#" + element),
                marker;
        //if we are dealing witha server-side request
        if(_this !== null){
            //Remove any Server exception messages not meant for the user
            marker = errorMessage.indexOf(":");
            errorMessage = errorMessage.slice(marker + 2, errorMessage.length);
            marker = errorMessage.indexOf(":");
            errorMessage = errorMessage.slice(marker + 2, errorMessage.length);
            //remove the (rethrown) string if it exists
            errorMessage = errorMessage.replace(/\(rethrown\)/g, '');
            $(_this).removeAttr("disabled");
        }
        errorElement.find("span").text(errorMessage);
        errorElement.slideDown('fast', function() {
            $(this).effect('highlight', 'slow');
        });
        $.fn.SMS.set.frameGrow(50, 'grow');
        $(".loadingImage").hide();
        return false;
    },
        //log = console.info,
        /**
         * Mappings from GSM 03.38 to Unicode as per
         * http://unicode.org/Public/MAPPINGS/ETSI/GSM0338.TXT
         */
            _isEncodeableInGsm0338 = function(utfString) {
                var gsmUtfMap = [
                    0x0040, 0x00A3, 0x0024, 0x00A5,
                    0x00E8, 0x00E9, 0x00F9, 0x00EC, 0x00F2, 0x00E7, 0x000A, 0x00D8,
                    0x00F8, 0x000D, 0x00C5, 0x00E5, 0x0394, 0x005F, 0x03A6, 0x0393,
                    0x039B, 0x03A9, 0x03A0, 0x03A8, 0x03A3, 0x0398, 0x039E, 0x00A0,
                    0x00C6, 0x00E6, 0x00DF, 0x00C9, 0x0020, 0x0021, 0x0022, 0x0023,
                    0x00A4, 0x0025, 0x0026, 0x0027, 0x0028, 0x0029, 0x002A, 0x002B,
                    0x002C, 0x002D, 0x002E, 0x002F, 0x0030, 0x0031, 0x0032, 0x0033,
                    0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003A, 0x003B,
                    0x003C, 0x003D, 0x003E, 0x003F, 0x00A1, 0x0041, 0x0042, 0x0043,
                    0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004A, 0x004B,
                    0x004C, 0x004D, 0x004E, 0x004F, 0x0050, 0x0051, 0x0052, 0x0053,
                    0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005A, 0x00C4,
                    0x00D6, 0x00D1, 0x00DC, 0x00A7, 0x00BF, 0x0061, 0x0062, 0x0063,
                    0x0064, 0x0065, 0x0066, 0x0067, 0x0068, 0x0069, 0x006A, 0x006B,
                    0x006C, 0x006D, 0x006E, 0x006F, 0x0070, 0x0071, 0x0072, 0x0073,
                    0x0074, 0x0075, 0x0076, 0x0077, 0x0078, 0x0079, 0x007A, 0x00E4,
                    0x00F6, 0x00F1, 0x00FC, 0x00E0
                ],
                        extGsmUtfMap = [
                            // { {Ext GSM,UTF} }
                            [ 0x0A, 0x000C ],
                            [ 0x14, 0x005E ],
                            [ 0x28, 0x007B ],
                            [ 0x29, 0x007D ],
                            [ 0x2F, 0x005C ],
                            [ 0x3C, 0x005B ],
                            [ 0x3D, 0x007E ],
                            [ 0x3E, 0x005D ],
                            [ 0x40, 0x007C ],
                            [ 0x65, 0x20AC ]
                        ],

                        utfChars = utfString.split('');

                // log("utfChars: %o", utfChars);

                outer: for (var i = 0; i < utfChars.length; i++) {
                    for (var g = 0; g < gsmUtfMap.length; g++) {
                        //log("Checking box %o against map %o", utfChars[i], gsmUtfMap[g]);
                        if (String.fromCharCode(gsmUtfMap[g]) == utfChars[i]) {
                            //log("match: map %o and box %o", gsmUtfMap[g], utfChars[i]) ;
                            continue outer;
                        }
                    }
                    for (var j = 0; j < extGsmUtfMap.length; j++) {
                        //log("Checking box %o against map %o", utfChars[i], extGsmUtfMap[j][1]);
                        if (String.fromCharCode(extGsmUtfMap[j][1]) == utfChars[i]) {
                            //log("match: extMap %o and box %o", extGsmUtfMap[j][1], utfChars[i]);
                            continue outer;
                        }
                    }
                    //log(false);
                    return false;
                }
                //log(true);
                return true;

            };


    //public methods
    return {
        error: {
            server: function(XMLHttpRequest, _this, element) {
                error($(XMLHttpRequest.responseText).find("u").eq(0).text(), _this, element);
            },
            dom: function(errorText) {
                 error(errorText, null, "errorFacebox");
            },
            isMessageLengthValid: false
        },
        isEncodeableInGsm0338: function(utfString){
           return _isEncodeableInGsm0338(utfString);
        }
    };
})($);