/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* eslint-disable */

/**
 * Created by harrywan on 2016/5/11.
 */
/**
 * jshashes - https://github.com/h2non/jshashes
 * Released under the "New BSD" license
 *
 * Algorithms specification:
 *
 * MD5 - http://www.ietf.org/rfc/rfc1321.txt
 * RIPEMD-160 - http://homes.esat.kuleuven.be/~bosselae/ripemd160.html
 * SHA1   - http://csrc.nist.gov/publications/fips/fips180-4/fips-180-4.pdf
 * SHA256 - http://csrc.nist.gov/publications/fips/fips180-4/fips-180-4.pdf
 * SHA512 - http://csrc.nist.gov/publications/fips/fips180-4/fips-180-4.pdf
 * HMAC - http://www.ietf.org/rfc/rfc2104.txt
 */

(function() {
    let Hashes;

    function utf8Encode(str) {
        let x; let y; let output = '';

        let i = -1;

        let l;

        if (str && str.length) {
            l = str.length;
            while ((i += 1) < l) {
                /* Decode utf-16 surrogate pairs */
                x = str.charCodeAt(i);
                y = i + 1 < l ? str.charCodeAt(i + 1) : 0;
                if (x >= 0xD800 && x <= 0xDBFF && y >= 0xDC00 && y <= 0xDFFF) {
                    x = 0x10000 + ((x & 0x03FF) << 10) + (y & 0x03FF);
                    i += 1;
                }
                /* Encode output as utf-8 */
                if (x <= 0x7F) {
                    output += String.fromCharCode(x);
                } else if (x <= 0x7FF) {
                    output += String.fromCharCode(0xC0 | ((x >>> 6) & 0x1F),
                        0x80 | (x & 0x3F));
                } else if (x <= 0xFFFF) {
                    output += String.fromCharCode(0xE0 | ((x >>> 12) & 0x0F),
                        0x80 | ((x >>> 6) & 0x3F),
                        0x80 | (x & 0x3F));
                } else if (x <= 0x1FFFFF) {
                    output += String.fromCharCode(0xF0 | ((x >>> 18) & 0x07),
                        0x80 | ((x >>> 12) & 0x3F),
                        0x80 | ((x >>> 6) & 0x3F),
                        0x80 | (x & 0x3F));
                }
            }
        }
        return output;
    }

    function utf8Decode(str) {
        let i; let ac; let c1; let c2; let c3; let arr = [];

        let l;
        i = ac = c1 = c2 = c3 = 0;

        if (str && str.length) {
            l = str.length;
            str += '';

            while (i < l) {
                c1 = str.charCodeAt(i);
                ac += 1;
                if (c1 < 128) {
                    arr[ac] = String.fromCharCode(c1);
                    i += 1;
                } else if (c1 > 191 && c1 < 224) {
                    c2 = str.charCodeAt(i + 1);
                    arr[ac] = String.fromCharCode(((c1 & 31) << 6) | (c2 & 63));
                    i += 2;
                } else {
                    c2 = str.charCodeAt(i + 1);
                    c3 = str.charCodeAt(i + 2);
                    arr[ac] = String.fromCharCode(((c1 & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                    i += 3;
                }
            }
        }
        return arr.join('');
    }

    /**
     * Add integers, wrapping at 2^32. This uses 16-bit operations internally
     * to work around bugs in some JS interpreters.
     */

    function safe_add(x, y) {
        let lsw = (x & 0xFFFF) + (y & 0xFFFF);

        let msw = (x >> 16) + (y >> 16) + (lsw >> 16);
        return (msw << 16) | (lsw & 0xFFFF);
    }

    /**
     * Bitwise rotate a 32-bit number to the left.
     */

    function bit_rol(num, cnt) {
        return (num << cnt) | (num >>> (32 - cnt));
    }

    /**
     * Convert a raw string to a hex string
     */

    function rstr2hex(input, hexcase) {
        let hex_tab = hexcase ? '0123456789ABCDEF' : '0123456789abcdef';

        let output = '';

        let x; let i = 0;

        let l = input.length;
        for (; i < l; i += 1) {
            x = input.charCodeAt(i);
            output += hex_tab.charAt((x >>> 4) & 0x0F) + hex_tab.charAt(x & 0x0F);
        }
        return output;
    }

    /**
     * Encode a string as utf-16
     */

    function str2rstr_utf16le(input) {
        let i; let l = input.length;

        let output = '';
        for (i = 0; i < l; i += 1) {
            output += String.fromCharCode(input.charCodeAt(i) & 0xFF, (input.charCodeAt(i) >>> 8) & 0xFF);
        }
        return output;
    }

    function str2rstr_utf16be(input) {
        let i; let l = input.length;

        let output = '';
        for (i = 0; i < l; i += 1) {
            output += String.fromCharCode((input.charCodeAt(i) >>> 8) & 0xFF, input.charCodeAt(i) & 0xFF);
        }
        return output;
    }

    /**
     * Convert an array of big-endian words to a string
     */

    function binb2rstr(input) {
        let i; let l = input.length * 32;

        let output = '';
        for (i = 0; i < l; i += 8) {
            output += String.fromCharCode((input[i >> 5] >>> (24 - i % 32)) & 0xFF);
        }
        return output;
    }

    /**
     * Convert an array of little-endian words to a string
     */

    function binl2rstr(input) {
        let i; let l = input.length * 32;

        let output = '';
        for (i = 0; i < l; i += 8) {
            output += String.fromCharCode((input[i >> 5] >>> (i % 32)) & 0xFF);
        }
        return output;
    }

    /**
     * Convert a raw string to an array of little-endian words
     * Characters >255 have their high-byte silently ignored.
     */

    function rstr2binl(input) {
        let i; let l = input.length * 8;

        let output = Array(input.length >> 2);

        let lo = output.length;
        for (i = 0; i < lo; i += 1) {
            output[i] = 0;
        }
        for (i = 0; i < l; i += 8) {
            output[i >> 5] |= (input.charCodeAt(i / 8) & 0xFF) << (i % 32);
        }
        return output;
    }

    /**
     * Convert a raw string to an array of big-endian words
     * Characters >255 have their high-byte silently ignored.
     */

    function rstr2binb(input) {
        let i; let l = input.length * 8;

        let output = Array(input.length >> 2);

        let lo = output.length;
        for (i = 0; i < lo; i += 1) {
            output[i] = 0;
        }
        for (i = 0; i < l; i += 8) {
            output[i >> 5] |= (input.charCodeAt(i / 8) & 0xFF) << (24 - i % 32);
        }
        return output;
    }

    /**
     * Convert a raw string to an arbitrary string encoding
     */

    function rstr2any(input, encoding) {
        let divisor = encoding.length;

        let remainders = Array();

        let i; let q; let x; let ld; let quotient; let dividend; let output; let full_length;

        /* Convert to an array of 16-bit big-endian values, forming the dividend */
        dividend = Array(Math.ceil(input.length / 2));
        ld = dividend.length;
        for (i = 0; i < ld; i += 1) {
            dividend[i] = (input.charCodeAt(i * 2) << 8) | input.charCodeAt(i * 2 + 1);
        }

        /**
         * Repeatedly perform a long division. The binary array forms the dividend,
         * the length of the encoding is the divisor. Once computed, the quotient
         * forms the dividend for the next step. We stop when the dividend is zerHashes.
         * All remainders are stored for later use.
         */
        while (dividend.length > 0) {
            quotient = Array();
            x = 0;
            for (i = 0; i < dividend.length; i += 1) {
                x = (x << 16) + dividend[i];
                q = Math.floor(x / divisor);
                x -= q * divisor;
                if (quotient.length > 0 || q > 0) {
                    quotient[quotient.length] = q;
                }
            }
            remainders[remainders.length] = x;
            dividend = quotient;
        }

        /* Convert the remainders to the output string */
        output = '';
        for (i = remainders.length - 1; i >= 0; i--) {
            output += encoding.charAt(remainders[i]);
        }

        /* Append leading zero equivalents */
        full_length = Math.ceil(input.length * 8 / (Math.log(encoding.length) / Math.log(2)));
        for (i = output.length; i < full_length; i += 1) {
            output = encoding[0] + output;
        }
        return output;
    }

    /**
     * Convert a raw string to a base-64 string
     */

    function rstr2b64(input, b64pad) {
        let tab = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';

        let output = '';

        let len = input.length;

        let i; let j; let triplet;
        b64pad = b64pad || '=';
        for (i = 0; i < len; i += 3) {
            triplet = (input.charCodeAt(i) << 16) | (i + 1 < len ? input.charCodeAt(i + 1) << 8 : 0) | (i + 2 < len ? input.charCodeAt(i + 2) : 0);
            for (j = 0; j < 4; j += 1) {
                if (i * 8 + j * 6 > input.length * 8) {
                    output += b64pad;
                } else {
                    output += tab.charAt((triplet >>> 6 * (3 - j)) & 0x3F);
                }
            }
        }
        return output;
    }

    Hashes = {
        /**
         * @property {String} version
         * @readonly
         */
        VERSION: '1.0.5',
        /**
         * @member Hashes
         * @class Base64
         * @constructor
         */
        Base64: function() {
            // private properties
            let tab = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';

            let pad = '=';
            // default pad according with the RFC standard

            let url = false;
            // URL encoding support @todo

            let utf8 = true; // by default enable UTF-8 support encoding

            // public method for encoding
            this.encode = function(input) {
                let i; let j; let triplet;

                let output = '';

                let len = input.length;

                pad = pad || '=';
                input = (utf8) ? utf8Encode(input) : input;

                for (i = 0; i < len; i += 3) {
                    triplet = (input.charCodeAt(i) << 16) | (i + 1 < len ? input.charCodeAt(i + 1) << 8 : 0) | (i + 2 < len ? input.charCodeAt(i + 2) : 0);
                    for (j = 0; j < 4; j += 1) {
                        if (i * 8 + j * 6 > len * 8) {
                            output += pad;
                        } else {
                            output += tab.charAt((triplet >>> 6 * (3 - j)) & 0x3F);
                        }
                    }
                }
                return output;
            };

            // public method for decoding
            this.decode = function(input) {
                // var b64 = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';
                let i; let o1; let o2; let o3; let h1; let h2; let h3; let h4; let bits; let ac;

                let dec = '';

                let arr = [];
                if (!input) {
                    return input;
                }

                i = ac = 0;
                input = input.replace(new RegExp('\\' + pad, 'gi'), ''); // use '='
                // input += '';

                do { // unpack four hexets into three octets using index points in b64
                    h1 = tab.indexOf(input.charAt(i += 1));
                    h2 = tab.indexOf(input.charAt(i += 1));
                    h3 = tab.indexOf(input.charAt(i += 1));
                    h4 = tab.indexOf(input.charAt(i += 1));

                    bits = h1 << 18 | h2 << 12 | h3 << 6 | h4;

                    o1 = bits >> 16 & 0xff;
                    o2 = bits >> 8 & 0xff;
                    o3 = bits & 0xff;
                    ac += 1;

                    if (h3 === 64) {
                        arr[ac] = String.fromCharCode(o1);
                    } else if (h4 === 64) {
                        arr[ac] = String.fromCharCode(o1, o2);
                    } else {
                        arr[ac] = String.fromCharCode(o1, o2, o3);
                    }
                } while (i < input.length);

                dec = arr.join('');
                dec = (utf8) ? utf8Decode(dec) : dec;

                return dec;
            };

            // set custom pad string
            this.setPad = function(str) {
                pad = str || pad;
                return this;
            };
            // set custom tab string characters
            this.setTab = function(str) {
                tab = str || tab;
                return this;
            };
            this.setUTF8 = function(bool) {
                if (typeof bool === 'boolean') {
                    utf8 = bool;
                }
                return this;
            };
        },

        /**
         * CRC-32 calculation
         * @member Hashes
         * @method CRC32
         * @static
         * @param {String} str Input String
         * @return {String}
         */
        CRC32: function(str) {
            let crc = 0;

            let x = 0;

            let y = 0;

            let table; let i; let iTop;
            str = utf8Encode(str);

            table = [
                '00000000 77073096 EE0E612C 990951BA 076DC419 706AF48F E963A535 9E6495A3 0EDB8832 ',
                '79DCB8A4 E0D5E91E 97D2D988 09B64C2B 7EB17CBD E7B82D07 90BF1D91 1DB71064 6AB020F2 F3B97148 ',
                '84BE41DE 1ADAD47D 6DDDE4EB F4D4B551 83D385C7 136C9856 646BA8C0 FD62F97A 8A65C9EC 14015C4F ',
                '63066CD9 FA0F3D63 8D080DF5 3B6E20C8 4C69105E D56041E4 A2677172 3C03E4D1 4B04D447 D20D85FD ',
                'A50AB56B 35B5A8FA 42B2986C DBBBC9D6 ACBCF940 32D86CE3 45DF5C75 DCD60DCF ABD13D59 26D930AC ',
                '51DE003A C8D75180 BFD06116 21B4F4B5 56B3C423 CFBA9599 B8BDA50F 2802B89E 5F058808 C60CD9B2 ',
                'B10BE924 2F6F7C87 58684C11 C1611DAB B6662D3D 76DC4190 01DB7106 98D220BC EFD5102A 71B18589 ',
                '06B6B51F 9FBFE4A5 E8B8D433 7807C9A2 0F00F934 9609A88E E10E9818 7F6A0DBB 086D3D2D 91646C97 ',
                'E6635C01 6B6B51F4 1C6C6162 856530D8 F262004E 6C0695ED 1B01A57B 8208F4C1 F50FC457 65B0D9C6 ',
                '12B7E950 8BBEB8EA FCB9887C 62DD1DDF 15DA2D49 8CD37CF3 FBD44C65 4DB26158 3AB551CE A3BC0074 ',
                'D4BB30E2 4ADFA541 3DD895D7 A4D1C46D D3D6F4FB 4369E96A 346ED9FC AD678846 DA60B8D0 44042D73 ',
                '33031DE5 AA0A4C5F DD0D7CC9 5005713C 270241AA BE0B1010 C90C2086 5768B525 206F85B3 B966D409 ',
                'CE61E49F 5EDEF90E 29D9C998 B0D09822 C7D7A8B4 59B33D17 2EB40D81 B7BD5C3B C0BA6CAD EDB88320 ',
                '9ABFB3B6 03B6E20C 74B1D29A EAD54739 9DD277AF 04DB2615 73DC1683 E3630B12 94643B84 0D6D6A3E ',
                '7A6A5AA8 E40ECF0B 9309FF9D 0A00AE27 7D079EB1 F00F9344 8708A3D2 1E01F268 6906C2FE F762575D ',
                '806567CB 196C3671 6E6B06E7 FED41B76 89D32BE0 10DA7A5A 67DD4ACC F9B9DF6F 8EBEEFF9 17B7BE43 ',
                '60B08ED5 D6D6A3E8 A1D1937E 38D8C2C4 4FDFF252 D1BB67F1 A6BC5767 3FB506DD 48B2364B D80D2BDA ',
                'AF0A1B4C 36034AF6 41047A60 DF60EFC3 A867DF55 316E8EEF 4669BE79 CB61B38C BC66831A 256FD2A0 ',
                '5268E236 CC0C7795 BB0B4703 220216B9 5505262F C5BA3BBE B2BD0B28 2BB45A92 5CB36A04 C2D7FFA7 ',
                'B5D0CF31 2CD99E8B 5BDEAE1D 9B64C2B0 EC63F226 756AA39C 026D930A 9C0906A9 EB0E363F 72076785 ',
                '05005713 95BF4A82 E2B87A14 7BB12BAE 0CB61B38 92D28E9B E5D5BE0D 7CDCEFB7 0BDBDF21 86D3D2D4 ',
                'F1D4E242 68DDB3F8 1FDA836E 81BE16CD F6B9265B 6FB077E1 18B74777 88085AE6 FF0F6A70 66063BCA ',
                '11010B5C 8F659EFF F862AE69 616BFFD3 166CCF45 A00AE278 D70DD2EE 4E048354 3903B3C2 A7672661 ',
                'D06016F7 4969474D 3E6E77DB AED16A4A D9D65ADC 40DF0B66 37D83BF0 A9BCAE53 DEBB9EC5 47B2CF7F ',
                '30B5FFE9 BDBDF21C CABAC28A 53B39330 24B4A3A6 BAD03605 CDD70693 54DE5729 23D967BF B3667A2E ',
                'C4614AB8 5D681B02 2A6F2B94 B40BBE37 C30C8EA1 5A05DF1B 2D02EF8D',
            ].join('');

            crc = crc ^ (-1);
            for (i = 0, iTop = str.length; i < iTop; i += 1) {
                y = (crc ^ str.charCodeAt(i)) & 0xFF;
                x = '0x' + table.substr(y * 9, 8);
                crc = (crc >>> 8) ^ x;
            }
            // always return a positive number (that's what >>> 0 does)
            return (crc ^ (-1)) >>> 0;
        },
        /**
         * @member Hashes
         * @class MD5
         * @constructor
         * @param {Object} [config]
         *
         * A JavaScript implementation of the RSA Data Security, Inc. MD5 Message
         * Digest Algorithm, as defined in RFC 1321.
         * Version 2.2 Copyright (C) Paul Johnston 1999 - 2009
         * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
         * See <http://pajhome.org.uk/crypt/md5> for more infHashes.
         */
        MD5: function(options) {
            /**
             * Private config properties. You may need to tweak these to be compatible with
             * the server-side, but the defaults work in most cases.
             * See {@link Hashes.MD5#method-setUpperCase} and {@link Hashes.SHA1#method-setUpperCase}
             */
            let hexcase = (options && typeof options.uppercase === 'boolean') ? options.uppercase : false;
            // hexadecimal output case format. false - lowercase; true - uppercase

            let b64pad = (options && typeof options.pad === 'string') ? options.pda : '=';
            // base-64 pad character. Defaults to '=' for strict RFC compliance

            let utf8 = (options && typeof options.utf8 === 'boolean') ? options.utf8 : true; // enable/disable utf8 encoding

            // privileged (public) methods
            this.hex = function(s) {
                return rstr2hex(rstr(s, utf8), hexcase);
            };
            this.b64 = function(s) {
                return rstr2b64(rstr(s), b64pad);
            };
            this.any = function(s, e) {
                return rstr2any(rstr(s, utf8), e);
            };
            this.raw = function(s) {
                return rstr(s, utf8);
            };
            this.hex_hmac = function(k, d) {
                return rstr2hex(rstr_hmac(k, d), hexcase);
            };
            this.b64_hmac = function(k, d) {
                return rstr2b64(rstr_hmac(k, d), b64pad);
            };
            this.any_hmac = function(k, d, e) {
                return rstr2any(rstr_hmac(k, d), e);
            };
            /**
             * Perform a simple self-test to see if the VM is working
             * @return {String} Hexadecimal hash sample
             */
            this.vm_test = function() {
                return hex('abc').toLowerCase() === '900150983cd24fb0d6963f7d28e17f72';
            };
            /**
             * Enable/disable uppercase hexadecimal returned string
             * @param {Boolean}
             * @return {Object} this
             */
            this.setUpperCase = function(a) {
                if (typeof a === 'boolean') {
                    hexcase = a;
                }
                return this;
            };
            /**
             * Defines a base64 pad string
             * @param {String} Pad
             * @return {Object} this
             */
            this.setPad = function(a) {
                b64pad = a || b64pad;
                return this;
            };
            /**
             * Defines a base64 pad string
             * @param {Boolean}
             * @return {Object} [this]
             */
            this.setUTF8 = function(a) {
                if (typeof a === 'boolean') {
                    utf8 = a;
                }
                return this;
            };

            // private methods

            /**
             * Calculate the MD5 of a raw string
             */

            function rstr(s) {
                s = (utf8) ? utf8Encode(s) : s;
                return binl2rstr(binl(rstr2binl(s), s.length * 8));
            }

            /**
             * Calculate the HMAC-MD5, of a key and some data (raw strings)
             */

            function rstr_hmac(key, data) {
                let bkey; let ipad; let opad; let hash; let i;

                key = (utf8) ? utf8Encode(key) : key;
                data = (utf8) ? utf8Encode(data) : data;
                bkey = rstr2binl(key);
                if (bkey.length > 16) {
                    bkey = binl(bkey, key.length * 8);
                }

                ipad = Array(16), opad = Array(16);
                for (i = 0; i < 16; i += 1) {
                    ipad[i] = bkey[i] ^ 0x36363636;
                    opad[i] = bkey[i] ^ 0x5C5C5C5C;
                }
                hash = binl(ipad.concat(rstr2binl(data)), 512 + data.length * 8);
                return binl2rstr(binl(opad.concat(hash), 512 + 128));
            }

            /**
             * Calculate the MD5 of an array of little-endian words, and a bit length.
             */

            function binl(x, len) {
                let i; let olda; let oldb; let oldc; let oldd;

                let a = 1732584193;

                let b = -271733879;

                let c = -1732584194;

                let d = 271733878;

                /* append padding */
                x[len >> 5] |= 0x80 << ((len) % 32);
                x[(((len + 64) >>> 9) << 4) + 14] = len;

                for (i = 0; i < x.length; i += 16) {
                    olda = a;
                    oldb = b;
                    oldc = c;
                    oldd = d;

                    a = md5_ff(a, b, c, d, x[i + 0], 7, -680876936);
                    d = md5_ff(d, a, b, c, x[i + 1], 12, -389564586);
                    c = md5_ff(c, d, a, b, x[i + 2], 17, 606105819);
                    b = md5_ff(b, c, d, a, x[i + 3], 22, -1044525330);
                    a = md5_ff(a, b, c, d, x[i + 4], 7, -176418897);
                    d = md5_ff(d, a, b, c, x[i + 5], 12, 1200080426);
                    c = md5_ff(c, d, a, b, x[i + 6], 17, -1473231341);
                    b = md5_ff(b, c, d, a, x[i + 7], 22, -45705983);
                    a = md5_ff(a, b, c, d, x[i + 8], 7, 1770035416);
                    d = md5_ff(d, a, b, c, x[i + 9], 12, -1958414417);
                    c = md5_ff(c, d, a, b, x[i + 10], 17, -42063);
                    b = md5_ff(b, c, d, a, x[i + 11], 22, -1990404162);
                    a = md5_ff(a, b, c, d, x[i + 12], 7, 1804603682);
                    d = md5_ff(d, a, b, c, x[i + 13], 12, -40341101);
                    c = md5_ff(c, d, a, b, x[i + 14], 17, -1502002290);
                    b = md5_ff(b, c, d, a, x[i + 15], 22, 1236535329);

                    a = md5_gg(a, b, c, d, x[i + 1], 5, -165796510);
                    d = md5_gg(d, a, b, c, x[i + 6], 9, -1069501632);
                    c = md5_gg(c, d, a, b, x[i + 11], 14, 643717713);
                    b = md5_gg(b, c, d, a, x[i + 0], 20, -373897302);
                    a = md5_gg(a, b, c, d, x[i + 5], 5, -701558691);
                    d = md5_gg(d, a, b, c, x[i + 10], 9, 38016083);
                    c = md5_gg(c, d, a, b, x[i + 15], 14, -660478335);
                    b = md5_gg(b, c, d, a, x[i + 4], 20, -405537848);
                    a = md5_gg(a, b, c, d, x[i + 9], 5, 568446438);
                    d = md5_gg(d, a, b, c, x[i + 14], 9, -1019803690);
                    c = md5_gg(c, d, a, b, x[i + 3], 14, -187363961);
                    b = md5_gg(b, c, d, a, x[i + 8], 20, 1163531501);
                    a = md5_gg(a, b, c, d, x[i + 13], 5, -1444681467);
                    d = md5_gg(d, a, b, c, x[i + 2], 9, -51403784);
                    c = md5_gg(c, d, a, b, x[i + 7], 14, 1735328473);
                    b = md5_gg(b, c, d, a, x[i + 12], 20, -1926607734);

                    a = md5_hh(a, b, c, d, x[i + 5], 4, -378558);
                    d = md5_hh(d, a, b, c, x[i + 8], 11, -2022574463);
                    c = md5_hh(c, d, a, b, x[i + 11], 16, 1839030562);
                    b = md5_hh(b, c, d, a, x[i + 14], 23, -35309556);
                    a = md5_hh(a, b, c, d, x[i + 1], 4, -1530992060);
                    d = md5_hh(d, a, b, c, x[i + 4], 11, 1272893353);
                    c = md5_hh(c, d, a, b, x[i + 7], 16, -155497632);
                    b = md5_hh(b, c, d, a, x[i + 10], 23, -1094730640);
                    a = md5_hh(a, b, c, d, x[i + 13], 4, 681279174);
                    d = md5_hh(d, a, b, c, x[i + 0], 11, -358537222);
                    c = md5_hh(c, d, a, b, x[i + 3], 16, -722521979);
                    b = md5_hh(b, c, d, a, x[i + 6], 23, 76029189);
                    a = md5_hh(a, b, c, d, x[i + 9], 4, -640364487);
                    d = md5_hh(d, a, b, c, x[i + 12], 11, -421815835);
                    c = md5_hh(c, d, a, b, x[i + 15], 16, 530742520);
                    b = md5_hh(b, c, d, a, x[i + 2], 23, -995338651);

                    a = md5_ii(a, b, c, d, x[i + 0], 6, -198630844);
                    d = md5_ii(d, a, b, c, x[i + 7], 10, 1126891415);
                    c = md5_ii(c, d, a, b, x[i + 14], 15, -1416354905);
                    b = md5_ii(b, c, d, a, x[i + 5], 21, -57434055);
                    a = md5_ii(a, b, c, d, x[i + 12], 6, 1700485571);
                    d = md5_ii(d, a, b, c, x[i + 3], 10, -1894986606);
                    c = md5_ii(c, d, a, b, x[i + 10], 15, -1051523);
                    b = md5_ii(b, c, d, a, x[i + 1], 21, -2054922799);
                    a = md5_ii(a, b, c, d, x[i + 8], 6, 1873313359);
                    d = md5_ii(d, a, b, c, x[i + 15], 10, -30611744);
                    c = md5_ii(c, d, a, b, x[i + 6], 15, -1560198380);
                    b = md5_ii(b, c, d, a, x[i + 13], 21, 1309151649);
                    a = md5_ii(a, b, c, d, x[i + 4], 6, -145523070);
                    d = md5_ii(d, a, b, c, x[i + 11], 10, -1120210379);
                    c = md5_ii(c, d, a, b, x[i + 2], 15, 718787259);
                    b = md5_ii(b, c, d, a, x[i + 9], 21, -343485551);

                    a = safe_add(a, olda);
                    b = safe_add(b, oldb);
                    c = safe_add(c, oldc);
                    d = safe_add(d, oldd);
                }
                return Array(a, b, c, d);
            }

            /**
             * These functions implement the four basic operations the algorithm uses.
             */

            function md5_cmn(q, a, b, x, s, t) {
                return safe_add(bit_rol(safe_add(safe_add(a, q), safe_add(x, t)), s), b);
            }

            function md5_ff(a, b, c, d, x, s, t) {
                return md5_cmn((b & c) | ((~b) & d), a, b, x, s, t);
            }

            function md5_gg(a, b, c, d, x, s, t) {
                return md5_cmn((b & d) | (c & (~d)), a, b, x, s, t);
            }

            function md5_hh(a, b, c, d, x, s, t) {
                return md5_cmn(b ^ c ^ d, a, b, x, s, t);
            }

            function md5_ii(a, b, c, d, x, s, t) {
                return md5_cmn(c ^ (b | (~d)), a, b, x, s, t);
            }
        },
        /**
         * @member Hashes
         * @class Hashes.SHA1
         * @param {Object} [config]
         * @constructor
         *
         * A JavaScript implementation of the Secure Hash Algorithm, SHA-1, as defined in FIPS 180-1
         * Version 2.2 Copyright Paul Johnston 2000 - 2009.
         * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
         * See http://pajhome.org.uk/crypt/md5 for details.
         */
        SHA1: function(options) {
            /**
             * Private config properties. You may need to tweak these to be compatible with
             * the server-side, but the defaults work in most cases.
             * See {@link Hashes.MD5#method-setUpperCase} and {@link Hashes.SHA1#method-setUpperCase}
             */
            let hexcase = (options && typeof options.uppercase === 'boolean') ? options.uppercase : false;
            // hexadecimal output case format. false - lowercase; true - uppercase

            let b64pad = (options && typeof options.pad === 'string') ? options.pda : '=';
            // base-64 pad character. Defaults to '=' for strict RFC compliance

            let utf8 = (options && typeof options.utf8 === 'boolean') ? options.utf8 : true; // enable/disable utf8 encoding

            // public methods
            this.hex = function(s) {
                return rstr2hex(rstr(s, utf8), hexcase);
            };
            this.b64 = function(s) {
                return rstr2b64(rstr(s, utf8), b64pad);
            };
            this.any = function(s, e) {
                return rstr2any(rstr(s, utf8), e);
            };
            this.raw = function(s) {
                return rstr(s, utf8);
            };
            this.hex_hmac = function(k, d) {
                return rstr2hex(rstr_hmac(k, d));
            };
            this.b64_hmac = function(k, d) {
                return rstr2b64(rstr_hmac(k, d), b64pad);
            };
            this.any_hmac = function(k, d, e) {
                return rstr2any(rstr_hmac(k, d), e);
            };
            /**
             * Perform a simple self-test to see if the VM is working
             * @return {String} Hexadecimal hash sample
             * @public
             */
            this.vm_test = function() {
                return hex('abc').toLowerCase() === '900150983cd24fb0d6963f7d28e17f72';
            };
            /**
             * @description Enable/disable uppercase hexadecimal returned string
             * @param {boolean}
             * @return {Object} this
             * @public
             */
            this.setUpperCase = function(a) {
                if (typeof a === 'boolean') {
                    hexcase = a;
                }
                return this;
            };
            /**
             * @description Defines a base64 pad string
             * @param {string} Pad
             * @return {Object} this
             * @public
             */
            this.setPad = function(a) {
                b64pad = a || b64pad;
                return this;
            };
            /**
             * @description Defines a base64 pad string
             * @param {boolean}
             * @return {Object} this
             * @public
             */
            this.setUTF8 = function(a) {
                if (typeof a === 'boolean') {
                    utf8 = a;
                }
                return this;
            };

            // private methods

            /**
             * Calculate the SHA-512 of a raw string
             */

            function rstr(s) {
                s = (utf8) ? utf8Encode(s) : s;
                return binb2rstr(binb(rstr2binb(s), s.length * 8));
            }

            /**
             * Calculate the HMAC-SHA1 of a key and some data (raw strings)
             */

            function rstr_hmac(key, data) {
                let bkey; let ipad; let opad; let i; let hash;
                key = (utf8) ? utf8Encode(key) : key;
                data = (utf8) ? utf8Encode(data) : data;
                bkey = rstr2binb(key);

                if (bkey.length > 16) {
                    bkey = binb(bkey, key.length * 8);
                }
                ipad = Array(16), opad = Array(16);
                for (i = 0; i < 16; i += 1) {
                    ipad[i] = bkey[i] ^ 0x36363636;
                    opad[i] = bkey[i] ^ 0x5C5C5C5C;
                }
                hash = binb(ipad.concat(rstr2binb(data)), 512 + data.length * 8);
                return binb2rstr(binb(opad.concat(hash), 512 + 160));
            }

            /**
             * Calculate the SHA-1 of an array of big-endian words, and a bit length
             */

            function binb(x, len) {
                let i; let j; let t; let olda; let oldb; let oldc; let oldd; let olde;

                let w = Array(80);

                let a = 1732584193;

                let b = -271733879;

                let c = -1732584194;

                let d = 271733878;

                let e = -1009589776;

                /* append padding */
                x[len >> 5] |= 0x80 << (24 - len % 32);
                x[((len + 64 >> 9) << 4) + 15] = len;

                for (i = 0; i < x.length; i += 16) {
                    olda = a,
                    oldb = b;
                    oldc = c;
                    oldd = d;
                    olde = e;

                    for (j = 0; j < 80; j += 1) {
                        if (j < 16) {
                            w[j] = x[i + j];
                        } else {
                            w[j] = bit_rol(w[j - 3] ^ w[j - 8] ^ w[j - 14] ^ w[j - 16], 1);
                        }
                        t = safe_add(safe_add(bit_rol(a, 5), sha1_ft(j, b, c, d)),
                            safe_add(safe_add(e, w[j]), sha1_kt(j)));
                        e = d;
                        d = c;
                        c = bit_rol(b, 30);
                        b = a;
                        a = t;
                    }

                    a = safe_add(a, olda);
                    b = safe_add(b, oldb);
                    c = safe_add(c, oldc);
                    d = safe_add(d, oldd);
                    e = safe_add(e, olde);
                }
                return Array(a, b, c, d, e);
            }

            /**
             * Perform the appropriate triplet combination function for the current
             * iteration
             */

            function sha1_ft(t, b, c, d) {
                if (t < 20) {
                    return (b & c) | ((~b) & d);
                }
                if (t < 40) {
                    return b ^ c ^ d;
                }
                if (t < 60) {
                    return (b & c) | (b & d) | (c & d);
                }
                return b ^ c ^ d;
            }

            /**
             * Determine the appropriate additive constant for the current iteration
             */

            function sha1_kt(t) {
                return (t < 20) ? 1518500249 : (t < 40) ? 1859775393 :
                    (t < 60) ? -1894007588 : -899497514;
            }
        },
        /**
         * @class Hashes.SHA256
         * @param {config}
         *
         * A JavaScript implementation of the Secure Hash Algorithm, SHA-256, as defined in FIPS 180-2
         * Version 2.2 Copyright Angel Marin, Paul Johnston 2000 - 2009.
         * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
         * See http://pajhome.org.uk/crypt/md5 for details.
         * Also http://anmar.eu.org/projects/jssha2/
         */
        SHA256: function(options) {
            /**
             * Private properties configuration variables. You may need to tweak these to be compatible with
             * the server-side, but the defaults work in most cases.
             * @see this.setUpperCase() method
             * @see this.setPad() method
             */
            let hexcase = (options && typeof options.uppercase === 'boolean') ? options.uppercase : false;
            // hexadecimal output case format. false - lowercase; true - uppercase  */

            let b64pad = (options && typeof options.pad === 'string') ? options.pda : '=';

            /* base-64 pad character. Default '=' for strict RFC compliance   */

            let utf8 = (options && typeof options.utf8 === 'boolean') ? options.utf8 : true;

            /* enable/disable utf8 encoding */

            let sha256_K;

            /* privileged (public) methods */
            this.hex = function(s) {
                return rstr2hex(rstr(s, utf8));
            };
            this.b64 = function(s) {
                return rstr2b64(rstr(s, utf8), b64pad);
            };
            this.any = function(s, e) {
                return rstr2any(rstr(s, utf8), e);
            };
            this.raw = function(s) {
                return rstr(s, utf8);
            };
            this.hex_hmac = function(k, d) {
                return rstr2hex(rstr_hmac(k, d));
            };
            this.b64_hmac = function(k, d) {
                return rstr2b64(rstr_hmac(k, d), b64pad);
            };
            this.any_hmac = function(k, d, e) {
                return rstr2any(rstr_hmac(k, d), e);
            };
            /**
             * Perform a simple self-test to see if the VM is working
             * @return {String} Hexadecimal hash sample
             * @public
             */
            this.vm_test = function() {
                return hex('abc').toLowerCase() === '900150983cd24fb0d6963f7d28e17f72';
            };
            /**
             * Enable/disable uppercase hexadecimal returned string
             * @param {boolean}
             * @return {Object} this
             * @public
             */
            this.setUpperCase = function(a) {
                if (typeof a === 'boolean') {
                    hexcase = a;
                }
                return this;
            };
            /**
             * @description Defines a base64 pad string
             * @param {string} Pad
             * @return {Object} this
             * @public
             */
            this.setPad = function(a) {
                b64pad = a || b64pad;
                return this;
            };
            /**
             * Defines a base64 pad string
             * @param {boolean}
             * @return {Object} this
             * @public
             */
            this.setUTF8 = function(a) {
                if (typeof a === 'boolean') {
                    utf8 = a;
                }
                return this;
            };

            // private methods

            /**
             * Calculate the SHA-512 of a raw string
             */

            function rstr(s, utf8) {
                s = (utf8) ? utf8Encode(s) : s;
                return binb2rstr(binb(rstr2binb(s), s.length * 8));
            }

            /**
             * Calculate the HMAC-sha256 of a key and some data (raw strings)
             */

            function rstr_hmac(key, data) {
                key = (utf8) ? utf8Encode(key) : key;
                data = (utf8) ? utf8Encode(data) : data;
                let hash; let i = 0;

                let bkey = rstr2binb(key);

                let ipad = Array(16);

                let opad = Array(16);

                if (bkey.length > 16) {
                    bkey = binb(bkey, key.length * 8);
                }

                for (; i < 16; i += 1) {
                    ipad[i] = bkey[i] ^ 0x36363636;
                    opad[i] = bkey[i] ^ 0x5C5C5C5C;
                }

                hash = binb(ipad.concat(rstr2binb(data)), 512 + data.length * 8);
                return binb2rstr(binb(opad.concat(hash), 512 + 256));
            }

            /*
             * Main sha256 function, with its support functions
             */

            function sha256_S(X, n) {
                return (X >>> n) | (X << (32 - n));
            }

            function sha256_R(X, n) {
                return (X >>> n);
            }

            function sha256_Ch(x, y, z) {
                return ((x & y) ^ ((~x) & z));
            }

            function sha256_Maj(x, y, z) {
                return ((x & y) ^ (x & z) ^ (y & z));
            }

            function sha256_Sigma0256(x) {
                return (sha256_S(x, 2) ^ sha256_S(x, 13) ^ sha256_S(x, 22));
            }

            function sha256_Sigma1256(x) {
                return (sha256_S(x, 6) ^ sha256_S(x, 11) ^ sha256_S(x, 25));
            }

            function sha256_Gamma0256(x) {
                return (sha256_S(x, 7) ^ sha256_S(x, 18) ^ sha256_R(x, 3));
            }

            function sha256_Gamma1256(x) {
                return (sha256_S(x, 17) ^ sha256_S(x, 19) ^ sha256_R(x, 10));
            }

            function sha256_Sigma0512(x) {
                return (sha256_S(x, 28) ^ sha256_S(x, 34) ^ sha256_S(x, 39));
            }

            function sha256_Sigma1512(x) {
                return (sha256_S(x, 14) ^ sha256_S(x, 18) ^ sha256_S(x, 41));
            }

            function sha256_Gamma0512(x) {
                return (sha256_S(x, 1) ^ sha256_S(x, 8) ^ sha256_R(x, 7));
            }

            function sha256_Gamma1512(x) {
                return (sha256_S(x, 19) ^ sha256_S(x, 61) ^ sha256_R(x, 6));
            }

            sha256_K = [
                1116352408, 1899447441, -1245643825, -373957723, 961987163, 1508970993, -1841331548, -1424204075, -670586216, 310598401, 607225278, 1426881987,
                1925078388, -2132889090, -1680079193, -1046744716, -459576895, -272742522,
                264347078, 604807628, 770255983, 1249150122, 1555081692, 1996064986, -1740746414, -1473132947, -1341970488, -1084653625, -958395405, -710438585,
                113926993, 338241895, 666307205, 773529912, 1294757372, 1396182291,
                1695183700, 1986661051, -2117940946, -1838011259, -1564481375, -1474664885, -1035236496, -949202525, -778901479, -694614492, -200395387, 275423344,
                430227734, 506948616, 659060556, 883997877, 958139571, 1322822218,
                1537002063, 1747873779, 1955562222, 2024104815, -2067236844, -1933114872, -1866530822, -1538233109, -1090935817, -965641998,
            ];

            function binb(m, l) {
                let HASH = [1779033703, -1150833019, 1013904242, -1521486534,
                    1359893119, -1694144372, 528734635, 1541459225,
                ];
                let W = new Array(64);
                let a; let b; let c; let d; let e; let f; let g; let h;
                let i; let j; let T1; let T2;

                /* append padding */
                m[l >> 5] |= 0x80 << (24 - l % 32);
                m[((l + 64 >> 9) << 4) + 15] = l;

                for (i = 0; i < m.length; i += 16) {
                    a = HASH[0];
                    b = HASH[1];
                    c = HASH[2];
                    d = HASH[3];
                    e = HASH[4];
                    f = HASH[5];
                    g = HASH[6];
                    h = HASH[7];

                    for (j = 0; j < 64; j += 1) {
                        if (j < 16) {
                            W[j] = m[j + i];
                        } else {
                            W[j] = safe_add(safe_add(safe_add(sha256_Gamma1256(W[j - 2]), W[j - 7]),
                                sha256_Gamma0256(W[j - 15])), W[j - 16]);
                        }

                        T1 = safe_add(safe_add(safe_add(safe_add(h, sha256_Sigma1256(e)), sha256_Ch(e, f, g)),
                            sha256_K[j]), W[j]);
                        T2 = safe_add(sha256_Sigma0256(a), sha256_Maj(a, b, c));
                        h = g;
                        g = f;
                        f = e;
                        e = safe_add(d, T1);
                        d = c;
                        c = b;
                        b = a;
                        a = safe_add(T1, T2);
                    }

                    HASH[0] = safe_add(a, HASH[0]);
                    HASH[1] = safe_add(b, HASH[1]);
                    HASH[2] = safe_add(c, HASH[2]);
                    HASH[3] = safe_add(d, HASH[3]);
                    HASH[4] = safe_add(e, HASH[4]);
                    HASH[5] = safe_add(f, HASH[5]);
                    HASH[6] = safe_add(g, HASH[6]);
                    HASH[7] = safe_add(h, HASH[7]);
                }
                return HASH;
            }
        },

        /**
         * @class Hashes.SHA512
         * @param {config}
         *
         * A JavaScript implementation of the Secure Hash Algorithm, SHA-512, as defined in FIPS 180-2
         * Version 2.2 Copyright Anonymous Contributor, Paul Johnston 2000 - 2009.
         * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
         * See http://pajhome.org.uk/crypt/md5 for details.
         */
        SHA512: function(options) {
            /**
             * Private properties configuration variables. You may need to tweak these to be compatible with
             * the server-side, but the defaults work in most cases.
             * @see this.setUpperCase() method
             * @see this.setPad() method
             */
            let hexcase = (options && typeof options.uppercase === 'boolean') ? options.uppercase : false;

            /* hexadecimal output case format. false - lowercase; true - uppercase  */

            let b64pad = (options && typeof options.pad === 'string') ? options.pda : '=';

            /* base-64 pad character. Default '=' for strict RFC compliance   */

            let utf8 = (options && typeof options.utf8 === 'boolean') ? options.utf8 : true;

            /* enable/disable utf8 encoding */

            let sha512_k;

            /* privileged (public) methods */
            this.hex = function(s) {
                return rstr2hex(rstr(s));
            };
            this.b64 = function(s) {
                return rstr2b64(rstr(s), b64pad);
            };
            this.any = function(s, e) {
                return rstr2any(rstr(s), e);
            };
            this.raw = function(s) {
                return rstr(s, utf8);
            };
            this.hex_hmac = function(k, d) {
                return rstr2hex(rstr_hmac(k, d));
            };
            this.b64_hmac = function(k, d) {
                return rstr2b64(rstr_hmac(k, d), b64pad);
            };
            this.any_hmac = function(k, d, e) {
                return rstr2any(rstr_hmac(k, d), e);
            };
            /**
             * Perform a simple self-test to see if the VM is working
             * @return {String} Hexadecimal hash sample
             * @public
             */
            this.vm_test = function() {
                return hex('abc').toLowerCase() === '900150983cd24fb0d6963f7d28e17f72';
            };
            /**
             * @description Enable/disable uppercase hexadecimal returned string
             * @param {boolean}
             * @return {Object} this
             * @public
             */
            this.setUpperCase = function(a) {
                if (typeof a === 'boolean') {
                    hexcase = a;
                }
                return this;
            };
            /**
             * @description Defines a base64 pad string
             * @param {string} Pad
             * @return {Object} this
             * @public
             */
            this.setPad = function(a) {
                b64pad = a || b64pad;
                return this;
            };
            /**
             * @description Defines a base64 pad string
             * @param {boolean}
             * @return {Object} this
             * @public
             */
            this.setUTF8 = function(a) {
                if (typeof a === 'boolean') {
                    utf8 = a;
                }
                return this;
            };

            /* private methods */

            /**
             * Calculate the SHA-512 of a raw string
             */

            function rstr(s) {
                s = (utf8) ? utf8Encode(s) : s;
                return binb2rstr(binb(rstr2binb(s), s.length * 8));
            }
            /*
             * Calculate the HMAC-SHA-512 of a key and some data (raw strings)
             */

            function rstr_hmac(key, data) {
                key = (utf8) ? utf8Encode(key) : key;
                data = (utf8) ? utf8Encode(data) : data;

                let hash; let i = 0;

                let bkey = rstr2binb(key);

                let ipad = Array(32);

                let opad = Array(32);

                if (bkey.length > 32) {
                    bkey = binb(bkey, key.length * 8);
                }

                for (; i < 32; i += 1) {
                    ipad[i] = bkey[i] ^ 0x36363636;
                    opad[i] = bkey[i] ^ 0x5C5C5C5C;
                }

                hash = binb(ipad.concat(rstr2binb(data)), 1024 + data.length * 8);
                return binb2rstr(binb(opad.concat(hash), 1024 + 512));
            }

            /**
             * Calculate the SHA-512 of an array of big-endian dwords, and a bit length
             */

            function binb(x, len) {
                let j; let i; let l;

                let W = new Array(80);

                let hash = new Array(16);

                // Initial hash values

                let H = [
                    new int64(0x6a09e667, -205731576),
                    new int64(-1150833019, -2067093701),
                    new int64(0x3c6ef372, -23791573),
                    new int64(-1521486534, 0x5f1d36f1),
                    new int64(0x510e527f, -1377402159),
                    new int64(-1694144372, 0x2b3e6c1f),
                    new int64(0x1f83d9ab, -79577749),
                    new int64(0x5be0cd19, 0x137e2179),
                ];

                let T1 = new int64(0, 0);

                let T2 = new int64(0, 0);

                let a = new int64(0, 0);

                let b = new int64(0, 0);

                let c = new int64(0, 0);

                let d = new int64(0, 0);

                let e = new int64(0, 0);

                let f = new int64(0, 0);

                let g = new int64(0, 0);

                let h = new int64(0, 0);

                // Temporary variables not specified by the document

                let s0 = new int64(0, 0);

                let s1 = new int64(0, 0);

                let Ch = new int64(0, 0);

                let Maj = new int64(0, 0);

                let r1 = new int64(0, 0);

                let r2 = new int64(0, 0);

                let r3 = new int64(0, 0);

                if (sha512_k === undefined) {
                    // SHA512 constants
                    sha512_k = [
                        new int64(0x428a2f98, -685199838), new int64(0x71374491, 0x23ef65cd),
                        new int64(-1245643825, -330482897), new int64(-373957723, -2121671748),
                        new int64(0x3956c25b, -213338824), new int64(0x59f111f1, -1241133031),
                        new int64(-1841331548, -1357295717), new int64(-1424204075, -630357736),
                        new int64(-670586216, -1560083902), new int64(0x12835b01, 0x45706fbe),
                        new int64(0x243185be, 0x4ee4b28c), new int64(0x550c7dc3, -704662302),
                        new int64(0x72be5d74, -226784913), new int64(-2132889090, 0x3b1696b1),
                        new int64(-1680079193, 0x25c71235), new int64(-1046744716, -815192428),
                        new int64(-459576895, -1628353838), new int64(-272742522, 0x384f25e3),
                        new int64(0xfc19dc6, -1953704523), new int64(0x240ca1cc, 0x77ac9c65),
                        new int64(0x2de92c6f, 0x592b0275), new int64(0x4a7484aa, 0x6ea6e483),
                        new int64(0x5cb0a9dc, -1119749164), new int64(0x76f988da, -2096016459),
                        new int64(-1740746414, -295247957), new int64(-1473132947, 0x2db43210),
                        new int64(-1341970488, -1728372417), new int64(-1084653625, -1091629340),
                        new int64(-958395405, 0x3da88fc2), new int64(-710438585, -1828018395),
                        new int64(0x6ca6351, -536640913), new int64(0x14292967, 0xa0e6e70),
                        new int64(0x27b70a85, 0x46d22ffc), new int64(0x2e1b2138, 0x5c26c926),
                        new int64(0x4d2c6dfc, 0x5ac42aed), new int64(0x53380d13, -1651133473),
                        new int64(0x650a7354, -1951439906), new int64(0x766a0abb, 0x3c77b2a8),
                        new int64(-2117940946, 0x47edaee6), new int64(-1838011259, 0x1482353b),
                        new int64(-1564481375, 0x4cf10364), new int64(-1474664885, -1136513023),
                        new int64(-1035236496, -789014639), new int64(-949202525, 0x654be30),
                        new int64(-778901479, -688958952), new int64(-694614492, 0x5565a910),
                        new int64(-200395387, 0x5771202a), new int64(0x106aa070, 0x32bbd1b8),
                        new int64(0x19a4c116, -1194143544), new int64(0x1e376c08, 0x5141ab53),
                        new int64(0x2748774c, -544281703), new int64(0x34b0bcb5, -509917016),
                        new int64(0x391c0cb3, -976659869), new int64(0x4ed8aa4a, -482243893),
                        new int64(0x5b9cca4f, 0x7763e373), new int64(0x682e6ff3, -692930397),
                        new int64(0x748f82ee, 0x5defb2fc), new int64(0x78a5636f, 0x43172f60),
                        new int64(-2067236844, -1578062990), new int64(-1933114872, 0x1a6439ec),
                        new int64(-1866530822, 0x23631e28), new int64(-1538233109, -561857047),
                        new int64(-1090935817, -1295615723), new int64(-965641998, -479046869),
                        new int64(-903397682, -366583396), new int64(-779700025, 0x21c0c207),
                        new int64(-354779690, -840897762), new int64(-176337025, -294727304),
                        new int64(0x6f067aa, 0x72176fba), new int64(0xa637dc5, -1563912026),
                        new int64(0x113f9804, -1090974290), new int64(0x1b710b35, 0x131c471b),
                        new int64(0x28db77f5, 0x23047d84), new int64(0x32caab7b, 0x40c72493),
                        new int64(0x3c9ebe0a, 0x15c9bebc), new int64(0x431d67c4, -1676669620),
                        new int64(0x4cc5d4be, -885112138), new int64(0x597f299c, -60457430),
                        new int64(0x5fcb6fab, 0x3ad6faec), new int64(0x6c44198c, 0x4a475817),
                    ];
                }

                for (i = 0; i < 80; i += 1) {
                    W[i] = new int64(0, 0);
                }

                // append padding to the source string. The format is described in the FIPS.
                x[len >> 5] |= 0x80 << (24 - (len & 0x1f));
                x[((len + 128 >> 10) << 5) + 31] = len;
                l = x.length;
                for (i = 0; i < l; i += 32) { // 32 dwords is the block size
                    int64copy(a, H[0]);
                    int64copy(b, H[1]);
                    int64copy(c, H[2]);
                    int64copy(d, H[3]);
                    int64copy(e, H[4]);
                    int64copy(f, H[5]);
                    int64copy(g, H[6]);
                    int64copy(h, H[7]);

                    for (j = 0; j < 16; j += 1) {
                        W[j].h = x[i + 2 * j];
                        W[j].l = x[i + 2 * j + 1];
                    }

                    for (j = 16; j < 80; j += 1) {
                        // sigma1
                        int64rrot(r1, W[j - 2], 19);
                        int64revrrot(r2, W[j - 2], 29);
                        int64shr(r3, W[j - 2], 6);
                        s1.l = r1.l ^ r2.l ^ r3.l;
                        s1.h = r1.h ^ r2.h ^ r3.h;
                        // sigma0
                        int64rrot(r1, W[j - 15], 1);
                        int64rrot(r2, W[j - 15], 8);
                        int64shr(r3, W[j - 15], 7);
                        s0.l = r1.l ^ r2.l ^ r3.l;
                        s0.h = r1.h ^ r2.h ^ r3.h;

                        int64add4(W[j], s1, W[j - 7], s0, W[j - 16]);
                    }

                    for (j = 0; j < 80; j += 1) {
                        // Ch
                        Ch.l = (e.l & f.l) ^ (~e.l & g.l);
                        Ch.h = (e.h & f.h) ^ (~e.h & g.h);

                        // Sigma1
                        int64rrot(r1, e, 14);
                        int64rrot(r2, e, 18);
                        int64revrrot(r3, e, 9);
                        s1.l = r1.l ^ r2.l ^ r3.l;
                        s1.h = r1.h ^ r2.h ^ r3.h;

                        // Sigma0
                        int64rrot(r1, a, 28);
                        int64revrrot(r2, a, 2);
                        int64revrrot(r3, a, 7);
                        s0.l = r1.l ^ r2.l ^ r3.l;
                        s0.h = r1.h ^ r2.h ^ r3.h;

                        // Maj
                        Maj.l = (a.l & b.l) ^ (a.l & c.l) ^ (b.l & c.l);
                        Maj.h = (a.h & b.h) ^ (a.h & c.h) ^ (b.h & c.h);

                        int64add5(T1, h, s1, Ch, sha512_k[j], W[j]);
                        int64add(T2, s0, Maj);

                        int64copy(h, g);
                        int64copy(g, f);
                        int64copy(f, e);
                        int64add(e, d, T1);
                        int64copy(d, c);
                        int64copy(c, b);
                        int64copy(b, a);
                        int64add(a, T1, T2);
                    }
                    int64add(H[0], H[0], a);
                    int64add(H[1], H[1], b);
                    int64add(H[2], H[2], c);
                    int64add(H[3], H[3], d);
                    int64add(H[4], H[4], e);
                    int64add(H[5], H[5], f);
                    int64add(H[6], H[6], g);
                    int64add(H[7], H[7], h);
                }

                // represent the hash as an array of 32-bit dwords
                for (i = 0; i < 8; i += 1) {
                    hash[2 * i] = H[i].h;
                    hash[2 * i + 1] = H[i].l;
                }
                return hash;
            }

            // A constructor for 64-bit numbers

            function int64(h, l) {
                this.h = h;
                this.l = l;
                // this.toString = int64toString;
            }

            // Copies src into dst, assuming both are 64-bit numbers

            function int64copy(dst, src) {
                dst.h = src.h;
                dst.l = src.l;
            }

            // Right-rotates a 64-bit number by shift
            // Won't handle cases of shift>=32
            // The function revrrot() is for that

            function int64rrot(dst, x, shift) {
                dst.l = (x.l >>> shift) | (x.h << (32 - shift));
                dst.h = (x.h >>> shift) | (x.l << (32 - shift));
            }

            // Reverses the dwords of the source and then rotates right by shift.
            // This is equivalent to rotation by 32+shift

            function int64revrrot(dst, x, shift) {
                dst.l = (x.h >>> shift) | (x.l << (32 - shift));
                dst.h = (x.l >>> shift) | (x.h << (32 - shift));
            }

            // Bitwise-shifts right a 64-bit number by shift
            // Won't handle shift>=32, but it's never needed in SHA512

            function int64shr(dst, x, shift) {
                dst.l = (x.l >>> shift) | (x.h << (32 - shift));
                dst.h = (x.h >>> shift);
            }

            // Adds two 64-bit numbers
            // Like the original implementation, does not rely on 32-bit operations

            function int64add(dst, x, y) {
                let w0 = (x.l & 0xffff) + (y.l & 0xffff);
                let w1 = (x.l >>> 16) + (y.l >>> 16) + (w0 >>> 16);
                let w2 = (x.h & 0xffff) + (y.h & 0xffff) + (w1 >>> 16);
                let w3 = (x.h >>> 16) + (y.h >>> 16) + (w2 >>> 16);
                dst.l = (w0 & 0xffff) | (w1 << 16);
                dst.h = (w2 & 0xffff) | (w3 << 16);
            }

            // Same, except with 4 addends. Works faster than adding them one by one.

            function int64add4(dst, a, b, c, d) {
                let w0 = (a.l & 0xffff) + (b.l & 0xffff) + (c.l & 0xffff) + (d.l & 0xffff);
                let w1 = (a.l >>> 16) + (b.l >>> 16) + (c.l >>> 16) + (d.l >>> 16) + (w0 >>> 16);
                let w2 = (a.h & 0xffff) + (b.h & 0xffff) + (c.h & 0xffff) + (d.h & 0xffff) + (w1 >>> 16);
                let w3 = (a.h >>> 16) + (b.h >>> 16) + (c.h >>> 16) + (d.h >>> 16) + (w2 >>> 16);
                dst.l = (w0 & 0xffff) | (w1 << 16);
                dst.h = (w2 & 0xffff) | (w3 << 16);
            }

            // Same, except with 5 addends

            function int64add5(dst, a, b, c, d, e) {
                let w0 = (a.l & 0xffff) + (b.l & 0xffff) + (c.l & 0xffff) + (d.l & 0xffff) + (e.l & 0xffff);

                let w1 = (a.l >>> 16) + (b.l >>> 16) + (c.l >>> 16) + (d.l >>> 16) + (e.l >>> 16) + (w0 >>> 16);

                let w2 = (a.h & 0xffff) + (b.h & 0xffff) + (c.h & 0xffff) + (d.h & 0xffff) + (e.h & 0xffff) + (w1 >>> 16);

                let w3 = (a.h >>> 16) + (b.h >>> 16) + (c.h >>> 16) + (d.h >>> 16) + (e.h >>> 16) + (w2 >>> 16);
                dst.l = (w0 & 0xffff) | (w1 << 16);
                dst.h = (w2 & 0xffff) | (w3 << 16);
            }
        },
        /**
         * @class Hashes.RMD160
         * @constructor
         * @param {Object} [config]
         *
         * A JavaScript implementation of the RIPEMD-160 Algorithm
         * Version 2.2 Copyright Jeremy Lin, Paul Johnston 2000 - 2009.
         * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
         * See http://pajhome.org.uk/crypt/md5 for details.
         * Also http://www.ocf.berkeley.edu/~jjlin/jsotp/
         */
        RMD160: function(options) {
            /**
             * Private properties configuration variables. You may need to tweak these to be compatible with
             * the server-side, but the defaults work in most cases.
             * @see this.setUpperCase() method
             * @see this.setPad() method
             */
            let hexcase = (options && typeof options.uppercase === 'boolean') ? options.uppercase : false;

            /* hexadecimal output case format. false - lowercase; true - uppercase  */

            let b64pad = (options && typeof options.pad === 'string') ? options.pda : '=';

            /* base-64 pad character. Default '=' for strict RFC compliance   */

            let utf8 = (options && typeof options.utf8 === 'boolean') ? options.utf8 : true;

            /* enable/disable utf8 encoding */

            let rmd160_r1 = [
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                7, 4, 13, 1, 10, 6, 15, 3, 12, 0, 9, 5, 2, 14, 11, 8,
                3, 10, 14, 4, 9, 15, 8, 1, 2, 7, 0, 6, 13, 11, 5, 12,
                1, 9, 11, 10, 0, 8, 12, 4, 13, 3, 7, 15, 14, 5, 6, 2,
                4, 0, 5, 9, 7, 12, 2, 10, 14, 1, 3, 8, 11, 6, 15, 13,
            ];

            let rmd160_r2 = [
                5, 14, 7, 0, 9, 2, 11, 4, 13, 6, 15, 8, 1, 10, 3, 12,
                6, 11, 3, 7, 0, 13, 5, 10, 14, 15, 8, 12, 4, 9, 1, 2,
                15, 5, 1, 3, 7, 14, 6, 9, 11, 8, 12, 2, 10, 0, 4, 13,
                8, 6, 4, 1, 3, 11, 15, 0, 5, 12, 2, 13, 9, 7, 10, 14,
                12, 15, 10, 4, 1, 5, 8, 7, 6, 2, 13, 14, 0, 3, 9, 11,
            ];

            let rmd160_s1 = [
                11, 14, 15, 12, 5, 8, 7, 9, 11, 13, 14, 15, 6, 7, 9, 8,
                7, 6, 8, 13, 11, 9, 7, 15, 7, 12, 15, 9, 11, 7, 13, 12,
                11, 13, 6, 7, 14, 9, 13, 15, 14, 8, 13, 6, 5, 12, 7, 5,
                11, 12, 14, 15, 14, 15, 9, 8, 9, 14, 5, 6, 8, 6, 5, 12,
                9, 15, 5, 11, 6, 8, 13, 12, 5, 12, 13, 14, 11, 8, 5, 6,
            ];

            let rmd160_s2 = [
                8, 9, 9, 11, 13, 15, 15, 5, 7, 7, 8, 11, 14, 14, 12, 6,
                9, 13, 15, 7, 12, 8, 9, 11, 7, 7, 12, 7, 6, 15, 13, 11,
                9, 7, 15, 11, 8, 6, 6, 14, 12, 13, 5, 14, 13, 13, 7, 5,
                15, 5, 8, 11, 14, 14, 6, 14, 6, 9, 12, 9, 12, 5, 15, 8,
                8, 5, 12, 9, 12, 5, 14, 6, 8, 13, 6, 5, 15, 13, 11, 11,
            ];

            /* privileged (public) methods */
            this.hex = function(s) {
                return rstr2hex(rstr(s, utf8));
            };
            this.b64 = function(s) {
                return rstr2b64(rstr(s, utf8), b64pad);
            };
            this.any = function(s, e) {
                return rstr2any(rstr(s, utf8), e);
            };
            this.raw = function(s) {
                return rstr(s, utf8);
            };
            this.hex_hmac = function(k, d) {
                return rstr2hex(rstr_hmac(k, d));
            };
            this.b64_hmac = function(k, d) {
                return rstr2b64(rstr_hmac(k, d), b64pad);
            };
            this.any_hmac = function(k, d, e) {
                return rstr2any(rstr_hmac(k, d), e);
            };
            /**
             * Perform a simple self-test to see if the VM is working
             * @return {String} Hexadecimal hash sample
             * @public
             */
            this.vm_test = function() {
                return hex('abc').toLowerCase() === '900150983cd24fb0d6963f7d28e17f72';
            };
            /**
             * @description Enable/disable uppercase hexadecimal returned string
             * @param {boolean}
             * @return {Object} this
             * @public
             */
            this.setUpperCase = function(a) {
                if (typeof a === 'boolean') {
                    hexcase = a;
                }
                return this;
            };
            /**
             * @description Defines a base64 pad string
             * @param {string} Pad
             * @return {Object} this
             * @public
             */
            this.setPad = function(a) {
                if (typeof a !== 'undefined') {
                    b64pad = a;
                }
                return this;
            };
            /**
             * @description Defines a base64 pad string
             * @param {boolean}
             * @return {Object} this
             * @public
             */
            this.setUTF8 = function(a) {
                if (typeof a === 'boolean') {
                    utf8 = a;
                }
                return this;
            };

            /* private methods */

            /**
             * Calculate the rmd160 of a raw string
             */

            function rstr(s) {
                s = (utf8) ? utf8Encode(s) : s;
                return binl2rstr(binl(rstr2binl(s), s.length * 8));
            }

            /**
             * Calculate the HMAC-rmd160 of a key and some data (raw strings)
             */

            function rstr_hmac(key, data) {
                key = (utf8) ? utf8Encode(key) : key;
                data = (utf8) ? utf8Encode(data) : data;
                let i; let hash;

                let bkey = rstr2binl(key);

                let ipad = Array(16);

                let opad = Array(16);

                if (bkey.length > 16) {
                    bkey = binl(bkey, key.length * 8);
                }

                for (i = 0; i < 16; i += 1) {
                    ipad[i] = bkey[i] ^ 0x36363636;
                    opad[i] = bkey[i] ^ 0x5C5C5C5C;
                }
                hash = binl(ipad.concat(rstr2binl(data)), 512 + data.length * 8);
                return binl2rstr(binl(opad.concat(hash), 512 + 160));
            }

            /**
             * Convert an array of little-endian words to a string
             */

            function binl2rstr(input) {
                let i; let output = '';

                let l = input.length * 32;
                for (i = 0; i < l; i += 8) {
                    output += String.fromCharCode((input[i >> 5] >>> (i % 32)) & 0xFF);
                }
                return output;
            }

            /**
             * Calculate the RIPE-MD160 of an array of little-endian words, and a bit length.
             */

            function binl(x, len) {
                let T; let j; let i; let l;

                let h0 = 0x67452301;

                let h1 = 0xefcdab89;

                let h2 = 0x98badcfe;

                let h3 = 0x10325476;

                let h4 = 0xc3d2e1f0;

                let A1; let B1; let C1; let D1; let E1;

                let A2; let B2; let C2; let D2; let E2;

                /* append padding */
                x[len >> 5] |= 0x80 << (len % 32);
                x[(((len + 64) >>> 9) << 4) + 14] = len;
                l = x.length;

                for (i = 0; i < l; i += 16) {
                    A1 = A2 = h0;
                    B1 = B2 = h1;
                    C1 = C2 = h2;
                    D1 = D2 = h3;
                    E1 = E2 = h4;
                    for (j = 0; j <= 79; j += 1) {
                        T = safe_add(A1, rmd160_f(j, B1, C1, D1));
                        T = safe_add(T, x[i + rmd160_r1[j]]);
                        T = safe_add(T, rmd160_K1(j));
                        T = safe_add(bit_rol(T, rmd160_s1[j]), E1);
                        A1 = E1;
                        E1 = D1;
                        D1 = bit_rol(C1, 10);
                        C1 = B1;
                        B1 = T;
                        T = safe_add(A2, rmd160_f(79 - j, B2, C2, D2));
                        T = safe_add(T, x[i + rmd160_r2[j]]);
                        T = safe_add(T, rmd160_K2(j));
                        T = safe_add(bit_rol(T, rmd160_s2[j]), E2);
                        A2 = E2;
                        E2 = D2;
                        D2 = bit_rol(C2, 10);
                        C2 = B2;
                        B2 = T;
                    }

                    T = safe_add(h1, safe_add(C1, D2));
                    h1 = safe_add(h2, safe_add(D1, E2));
                    h2 = safe_add(h3, safe_add(E1, A2));
                    h3 = safe_add(h4, safe_add(A1, B2));
                    h4 = safe_add(h0, safe_add(B1, C2));
                    h0 = T;
                }
                return [h0, h1, h2, h3, h4];
            }

            // specific algorithm methods

            function rmd160_f(j, x, y, z) {
                return (j >= 0 && j <= 15) ? (x ^ y ^ z) :
                    (j >= 16 && j <= 31) ? (x & y) | (~x & z) :
                        (j >= 32 && j <= 47) ? (x | ~y) ^ z :
                            (j >= 48 && j <= 63) ? (x & z) | (y & ~z) :
                                (j >= 64 && j <= 79) ? x ^ (y | ~z) :
                                    'rmd160_f: j out of range';
            }

            function rmd160_K1(j) {
                return (j >= 0 && j <= 15) ? 0x00000000 :
                    (j >= 16 && j <= 31) ? 0x5a827999 :
                        (j >= 32 && j <= 47) ? 0x6ed9eba1 :
                            (j >= 48 && j <= 63) ? 0x8f1bbcdc :
                                (j >= 64 && j <= 79) ? 0xa953fd4e :
                                    'rmd160_K1: j out of range';
            }

            function rmd160_K2(j) {
                return (j >= 0 && j <= 15) ? 0x50a28be6 :
                    (j >= 16 && j <= 31) ? 0x5c4dd124 :
                        (j >= 32 && j <= 47) ? 0x6d703ef3 :
                            (j >= 48 && j <= 63) ? 0x7a6d76e9 :
                                (j >= 64 && j <= 79) ? 0x00000000 :
                                    'rmd160_K2: j out of range';
            }
        },
    };

    // exposes Hashes
    // (function(window, undefined) {
    //    var freeExports = false;
    //    if (typeof exports === 'object') {
    //        freeExports = exports;
    //        if (exports && typeof global === 'object' && global && global === global.global) {
    //            window = global;
    //        }
    //    }
    //
    //    if (typeof define === 'function' && typeof define.amd === 'object' && define.amd) {
    //        // define as an anonymous module, so, through path mapping, it can be aliased
    //        define(function() {
    //            return Hashes;
    //        });
    //    } else if (freeExports) {
    //        // in Node.js or RingoJS v0.8.0+
    //        if (typeof module === 'object' && module && module.exports === freeExports) {
    //            module.exports = Hashes;
    //        }
    //        // in Narwhal or RingoJS v0.7.0-
    //        else {
    //            freeExports.Hashes = Hashes;
    //        }
    //    } else {
    //        // in a browser or Rhino
    //        window.Hashes = Hashes;
    //    }
    // }(this));

    window.Hashes = Hashes;
})(window); // IIFE
export var Hashes = window.Hashes;

/**
 *var $pem = "-----BEGIN PUBLIC KEY-----MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMYQWDqtLgDKlQvWzacGeBMQpbicd/uoXAvgLNpFZLM7zuYFDhrYncRsl8LIHK0K3f7e1aFmUVgM4LrKU2WFIw0CAwEAAQ==-----END PUBLIC KEY-----";
 var $key = RSA.getPublicKey($pem);
 RSA.encrypt(data_field.value, $key);
 }
 */

// Copyright (c) 2005  Tom Wu
// All Rights Reserved.
// See "LICENSE" for details.
// Basic JavaScript BN library - subset useful for RSA encryption.

// Bits per digit
let dbits;

// JavaScript engine analysis
let canary = 0xdeadbeefcafe;
let j_lm = ((canary & 0xffffff) == 0xefcafe);

// (public) Constructor

function BigInteger(a, b, c) {
    if (a != null) {
        if (typeof a == 'number') this.fromNumber(a, b, c);
        else if (b == null && typeof a != 'string') this.fromString(a, 256);
        else this.fromString(a, b);
    }
}

// return new, unset BigInteger

function nbi() {
    return new BigInteger(null);
}

// am: Compute w_j += (x*this_i), propagate carries,
// c is initial carry, returns final carry.
// c < 3*dvalue, x < 2*dvalue, this_i < dvalue
// We need to select the fastest one that works in this environment.
// am1: use a single mult and divide to get the high bits,
// max digit bits should be 26 because
// max internal value = 2*dvalue^2-2*dvalue (< 2^53)

function am1(i, x, w, j, c, n) {
    while (--n >= 0) {
        let v = x * this[i++] + w[j] + c;
        c = Math.floor(v / 0x4000000);
        w[j++] = v & 0x3ffffff;
    }
    return c;
}
// am2 avoids a big mult-and-extract completely.
// Max digit bits should be <= 30 because we do bitwise ops
// on values up to 2*hdvalue^2-hdvalue-1 (< 2^31)

function am2(i, x, w, j, c, n) {
    let xl = x & 0x7fff;

    let xh = x >> 15;
    while (--n >= 0) {
        let l = this[i] & 0x7fff;
        let h = this[i++] >> 15;
        let m = xh * l + h * xl;
        l = xl * l + ((m & 0x7fff) << 15) + w[j] + (c & 0x3fffffff);
        c = (l >>> 30) + (m >>> 15) + xh * h + (c >>> 30);
        w[j++] = l & 0x3fffffff;
    }
    return c;
}
// Alternately, set max digit bits to 28 since some
// browsers slow down when dealing with 32-bit numbers.

function am3(i, x, w, j, c, n) {
    let xl = x & 0x3fff;

    let xh = x >> 14;
    while (--n >= 0) {
        let l = this[i] & 0x3fff;
        let h = this[i++] >> 14;
        let m = xh * l + h * xl;
        l = xl * l + ((m & 0x3fff) << 14) + w[j] + c;
        c = (l >> 28) + (m >> 14) + xh * h;
        w[j++] = l & 0xfffffff;
    }
    return c;
}
if (j_lm && (navigator.appName == 'Microsoft Internet Explorer')) {
    BigInteger.prototype.am = am2;
    dbits = 30;
} else if (j_lm && (navigator.appName != 'Netscape')) {
    BigInteger.prototype.am = am1;
    dbits = 26;
} else { // Mozilla/Netscape seems to prefer am3
    BigInteger.prototype.am = am3;
    dbits = 28;
}

BigInteger.prototype.DB = dbits;
BigInteger.prototype.DM = ((1 << dbits) - 1);
BigInteger.prototype.DV = (1 << dbits);

let BI_FP = 52;
BigInteger.prototype.FV = Math.pow(2, BI_FP);
BigInteger.prototype.F1 = BI_FP - dbits;
BigInteger.prototype.F2 = 2 * dbits - BI_FP;

// Digit conversions
let BI_RM = '0123456789abcdefghijklmnopqrstuvwxyz';
let BI_RC = new Array();
let rr; let vv;
rr = '0'.charCodeAt(0);
for (vv = 0; vv <= 9; ++vv) BI_RC[rr++] = vv;
rr = 'a'.charCodeAt(0);
for (vv = 10; vv < 36; ++vv) BI_RC[rr++] = vv;
rr = 'A'.charCodeAt(0);
for (vv = 10; vv < 36; ++vv) BI_RC[rr++] = vv;

function int2char(n) {
    return BI_RM.charAt(n);
}

function intAt(s, i) {
    let c = BI_RC[s.charCodeAt(i)];
    return (c == null) ? -1 : c;
}

// (protected) copy this to r

function bnpCopyTo(r) {
    for (let i = this.t - 1; i >= 0; --i) r[i] = this[i];
    r.t = this.t;
    r.s = this.s;
}

// (protected) set from integer value x, -DV <= x < DV

function bnpFromInt(x) {
    this.t = 1;
    this.s = (x < 0) ? -1 : 0;
    if (x > 0) this[0] = x;
    else if (x < -1) this[0] = x + DV;
    else this.t = 0;
}

// return bigint initialized to value

function nbv(i) {
    let r = nbi();
    r.fromInt(i);
    return r;
}

// (protected) set from string and radix

function bnpFromString(s, b) {
    let k;
    if (b == 16) k = 4;
    else if (b == 8) k = 3;
    else if (b == 256) k = 8; // byte array
    else if (b == 2) k = 1;
    else if (b == 32) k = 5;
    else if (b == 4) k = 2;
    else {
        this.fromRadix(s, b);
        return;
    }
    this.t = 0;
    this.s = 0;
    let i = s.length;

    let mi = false;

    let sh = 0;
    while (--i >= 0) {
        let x = (k == 8) ? s[i] & 0xff : intAt(s, i);
        if (x < 0) {
            if (s.charAt(i) == '-') mi = true;
            continue;
        }
        mi = false;
        if (sh == 0) this[this.t++] = x;
        else if (sh + k > this.DB) {
            this[this.t - 1] |= (x & ((1 << (this.DB - sh)) - 1)) << sh;
            this[this.t++] = (x >> (this.DB - sh));
        } else this[this.t - 1] |= x << sh;
        sh += k;
        if (sh >= this.DB) sh -= this.DB;
    }
    if (k == 8 && (s[0] & 0x80) != 0) {
        this.s = -1;
        if (sh > 0) this[this.t - 1] |= ((1 << (this.DB - sh)) - 1) << sh;
    }
    this.clamp();
    if (mi) BigInteger.ZERO.subTo(this, this);
}

// (protected) clamp off excess high words

function bnpClamp() {
    let c = this.s & this.DM;
    while (this.t > 0 && this[this.t - 1] == c)--this.t;
}

// (public) return string representation in given radix

function bnToString(b) {
    if (this.s < 0) return '-' + this.negate().toString(b);
    let k;
    if (b == 16) k = 4;
    else if (b == 8) k = 3;
    else if (b == 2) k = 1;
    else if (b == 32) k = 5;
    else if (b == 64) k = 6;
    else if (b == 4) k = 2;
    else return this.toRadix(b);
    let km = (1 << k) - 1;

    let d; let m = false;

    let r = '';

    let i = this.t;
    let p = this.DB - (i * this.DB) % k;
    if (i-- > 0) {
        if (p < this.DB && (d = this[i] >> p) > 0) {
            m = true;
            r = int2char(d);
        }
        while (i >= 0) {
            if (p < k) {
                d = (this[i] & ((1 << p) - 1)) << (k - p);
                d |= this[--i] >> (p += this.DB - k);
            } else {
                d = (this[i] >> (p -= k)) & km;
                if (p <= 0) {
                    p += this.DB;
                    --i;
                }
            }
            if (d > 0) m = true;
            if (m) r += int2char(d);
        }
    }
    return m ? r : '0';
}

// (public) -this

function bnNegate() {
    let r = nbi();
    BigInteger.ZERO.subTo(this, r);
    return r;
}

// (public) |this|

function bnAbs() {
    return (this.s < 0) ? this.negate() : this;
}

// (public) return + if this > a, - if this < a, 0 if equal

function bnCompareTo(a) {
    let r = this.s - a.s;
    if (r != 0) return r;
    let i = this.t;
    r = i - a.t;
    if (r != 0) return r;
    while (--i >= 0) if ((r = this[i] - a[i]) != 0) return r;
    return 0;
}

// returns bit length of the integer x

function nbits(x) {
    let r = 1;

    let t;
    if ((t = x >>> 16) != 0) {
        x = t;
        r += 16;
    }
    if ((t = x >> 8) != 0) {
        x = t;
        r += 8;
    }
    if ((t = x >> 4) != 0) {
        x = t;
        r += 4;
    }
    if ((t = x >> 2) != 0) {
        x = t;
        r += 2;
    }
    if ((t = x >> 1) != 0) {
        x = t;
        r += 1;
    }
    return r;
}

// (public) return the number of bits in "this"

function bnBitLength() {
    if (this.t <= 0) return 0;
    return this.DB * (this.t - 1) + nbits(this[this.t - 1] ^ (this.s & this.DM));
}

// (protected) r = this << n*DB

function bnpDLShiftTo(n, r) {
    let i;
    for (i = this.t - 1; i >= 0; --i) r[i + n] = this[i];
    for (i = n - 1; i >= 0; --i) r[i] = 0;
    r.t = this.t + n;
    r.s = this.s;
}

// (protected) r = this >> n*DB

function bnpDRShiftTo(n, r) {
    for (let i = n; i < this.t; ++i) r[i - n] = this[i];
    r.t = Math.max(this.t - n, 0);
    r.s = this.s;
}

// (protected) r = this << n

function bnpLShiftTo(n, r) {
    let bs = n % this.DB;
    let cbs = this.DB - bs;
    let bm = (1 << cbs) - 1;
    let ds = Math.floor(n / this.DB);

    let c = (this.s << bs) & this.DM;

    let i;
    for (i = this.t - 1; i >= 0; --i) {
        r[i + ds + 1] = (this[i] >> cbs) | c;
        c = (this[i] & bm) << bs;
    }
    for (i = ds - 1; i >= 0; --i) r[i] = 0;
    r[ds] = c;
    r.t = this.t + ds + 1;
    r.s = this.s;
    r.clamp();
}

// (protected) r = this >> n

function bnpRShiftTo(n, r) {
    r.s = this.s;
    let ds = Math.floor(n / this.DB);
    if (ds >= this.t) {
        r.t = 0;
        return;
    }
    let bs = n % this.DB;
    let cbs = this.DB - bs;
    let bm = (1 << bs) - 1;
    r[0] = this[ds] >> bs;
    for (let i = ds + 1; i < this.t; ++i) {
        r[i - ds - 1] |= (this[i] & bm) << cbs;
        r[i - ds] = this[i] >> bs;
    }
    if (bs > 0) r[this.t - ds - 1] |= (this.s & bm) << cbs;
    r.t = this.t - ds;
    r.clamp();
}

// (protected) r = this - a

function bnpSubTo(a, r) {
    let i = 0;

    let c = 0;

    let m = Math.min(a.t, this.t);
    while (i < m) {
        c += this[i] - a[i];
        r[i++] = c & this.DM;
        c >>= this.DB;
    }
    if (a.t < this.t) {
        c -= a.s;
        while (i < this.t) {
            c += this[i];
            r[i++] = c & this.DM;
            c >>= this.DB;
        }
        c += this.s;
    } else {
        c += this.s;
        while (i < a.t) {
            c -= a[i];
            r[i++] = c & this.DM;
            c >>= this.DB;
        }
        c -= a.s;
    }
    r.s = (c < 0) ? -1 : 0;
    if (c < -1) r[i++] = this.DV + c;
    else if (c > 0) r[i++] = c;
    r.t = i;
    r.clamp();
}

// (protected) r = this * a, r != this,a (HAC 14.12)
// "this" should be the larger one if appropriate.

function bnpMultiplyTo(a, r) {
    let x = this.abs();

    let y = a.abs();
    let i = x.t;
    r.t = i + y.t;
    while (--i >= 0) r[i] = 0;
    for (i = 0; i < y.t; ++i) r[i + x.t] = x.am(0, y[i], r, i, 0, x.t);
    r.s = 0;
    r.clamp();
    if (this.s != a.s) BigInteger.ZERO.subTo(r, r);
}

// (protected) r = this^2, r != this (HAC 14.16)

function bnpSquareTo(r) {
    let x = this.abs();
    let i = r.t = 2 * x.t;
    while (--i >= 0) r[i] = 0;
    for (i = 0; i < x.t - 1; ++i) {
        let c = x.am(i, x[i], r, 2 * i, 0, 1);
        if ((r[i + x.t] += x.am(i + 1, 2 * x[i], r, 2 * i + 1, c, x.t - i - 1)) >= x.DV) {
            r[i + x.t] -= x.DV;
            r[i + x.t + 1] = 1;
        }
    }
    if (r.t > 0) r[r.t - 1] += x.am(i, x[i], r, 2 * i, 0, 1);
    r.s = 0;
    r.clamp();
}

// (protected) divide this by m, quotient and remainder to q, r (HAC 14.20)
// r != q, this != m.  q or r may be null.

function bnpDivRemTo(m, q, r) {
    let pm = m.abs();
    if (pm.t <= 0) return;
    let pt = this.abs();
    if (pt.t < pm.t) {
        if (q != null) q.fromInt(0);
        if (r != null) this.copyTo(r);
        return;
    }
    if (r == null) r = nbi();
    let y = nbi();

    let ts = this.s;

    let ms = m.s;
    let nsh = this.DB - nbits(pm[pm.t - 1]); // normalize modulus
    if (nsh > 0) {
        pm.lShiftTo(nsh, y);
        pt.lShiftTo(nsh, r);
    } else {
        pm.copyTo(y);
        pt.copyTo(r);
    }
    let ys = y.t;
    let y0 = y[ys - 1];
    if (y0 == 0) return;
    let yt = y0 * (1 << this.F1) + ((ys > 1) ? y[ys - 2] >> this.F2 : 0);
    let d1 = this.FV / yt;

    let d2 = (1 << this.F1) / yt;

    let e = 1 << this.F2;
    let i = r.t;

    let j = i - ys;

    let t = (q == null) ? nbi() : q;
    y.dlShiftTo(j, t);
    if (r.compareTo(t) >= 0) {
        r[r.t++] = 1;
        r.subTo(t, r);
    }
    BigInteger.ONE.dlShiftTo(ys, t);
    t.subTo(y, y); // "negative" y so we can replace sub with am later
    while (y.t < ys) y[y.t++] = 0;
    while (--j >= 0) {
        // Estimate quotient digit
        let qd = (r[--i] == y0) ? this.DM : Math.floor(r[i] * d1 + (r[i - 1] + e) * d2);
        if ((r[i] += y.am(0, qd, r, j, 0, ys)) < qd) { // Try it out
            y.dlShiftTo(j, t);
            r.subTo(t, r);
            while (r[i] < --qd) r.subTo(t, r);
        }
    }
    if (q != null) {
        r.drShiftTo(ys, q);
        if (ts != ms) BigInteger.ZERO.subTo(q, q);
    }
    r.t = ys;
    r.clamp();
    if (nsh > 0) r.rShiftTo(nsh, r); // Denormalize remainder
    if (ts < 0) BigInteger.ZERO.subTo(r, r);
}

// (public) this mod a

function bnMod(a) {
    let r = nbi();
    this.abs().divRemTo(a, null, r);
    if (this.s < 0 && r.compareTo(BigInteger.ZERO) > 0) a.subTo(r, r);
    return r;
}

// Modular reduction using "classic" algorithm

function Classic(m) {
    this.m = m;
}

function cConvert(x) {
    if (x.s < 0 || x.compareTo(this.m) >= 0) return x.mod(this.m);
    else return x;
}

function cRevert(x) {
    return x;
}

function cReduce(x) {
    x.divRemTo(this.m, null, x);
}

function cMulTo(x, y, r) {
    x.multiplyTo(y, r);
    this.reduce(r);
}

function cSqrTo(x, r) {
    x.squareTo(r);
    this.reduce(r);
}

Classic.prototype.convert = cConvert;
Classic.prototype.revert = cRevert;
Classic.prototype.reduce = cReduce;
Classic.prototype.mulTo = cMulTo;
Classic.prototype.sqrTo = cSqrTo;

// (protected) return "-1/this % 2^DB"; useful for Mont. reduction
// justification:
//         xy == 1 (mod m)
//         xy =  1+km
//   xy(2-xy) = (1+km)(1-km)
// x[y(2-xy)] = 1-k^2m^2
// x[y(2-xy)] == 1 (mod m^2)
// if y is 1/x mod m, then y(2-xy) is 1/x mod m^2
// should reduce x and y(2-xy) by m^2 at each step to keep size bounded.
// JS multiply "overflows" differently from C/C++, so care is needed here.

function bnpInvDigit() {
    if (this.t < 1) return 0;
    let x = this[0];
    if ((x & 1) == 0) return 0;
    let y = x & 3; // y == 1/x mod 2^2
    y = (y * (2 - (x & 0xf) * y)) & 0xf; // y == 1/x mod 2^4
    y = (y * (2 - (x & 0xff) * y)) & 0xff; // y == 1/x mod 2^8
    y = (y * (2 - (((x & 0xffff) * y) & 0xffff))) & 0xffff; // y == 1/x mod 2^16
    // last step - calculate inverse mod DV directly;
    // assumes 16 < DB <= 32 and assumes ability to handle 48-bit ints
    y = (y * (2 - x * y % this.DV)) % this.DV; // y == 1/x mod 2^dbits
    // we really want the negative inverse, and -DV < y < DV
    return (y > 0) ? this.DV - y : -y;
}

// Montgomery reduction

function Montgomery(m) {
    this.m = m;
    this.mp = m.invDigit();
    this.mpl = this.mp & 0x7fff;
    this.mph = this.mp >> 15;
    this.um = (1 << (m.DB - 15)) - 1;
    this.mt2 = 2 * m.t;
}

// xR mod m

function montConvert(x) {
    let r = nbi();
    x.abs().dlShiftTo(this.m.t, r);
    r.divRemTo(this.m, null, r);
    if (x.s < 0 && r.compareTo(BigInteger.ZERO) > 0) this.m.subTo(r, r);
    return r;
}

// x/R mod m

function montRevert(x) {
    let r = nbi();
    x.copyTo(r);
    this.reduce(r);
    return r;
}

// x = x/R mod m (HAC 14.32)

function montReduce(x) {
    while (x.t <= this.mt2) // pad x so am has enough room later
    {
        x[x.t++] = 0;
    }
    for (let i = 0; i < this.m.t; ++i) {
        // faster way of calculating u0 = x[i]*mp mod DV
        let j = x[i] & 0x7fff;
        let u0 = (j * this.mpl + (((j * this.mph + (x[i] >> 15) * this.mpl) & this.um) << 15)) & x.DM;
        // use am to combine the multiply-shift-add into one call
        j = i + this.m.t;
        x[j] += this.m.am(0, u0, x, i, 0, this.m.t);
        // propagate carry
        while (x[j] >= x.DV) {
            x[j] -= x.DV;
            x[++j]++;
        }
    }
    x.clamp();
    x.drShiftTo(this.m.t, x);
    if (x.compareTo(this.m) >= 0) x.subTo(this.m, x);
}

// r = "x^2/R mod m"; x != r

function montSqrTo(x, r) {
    x.squareTo(r);
    this.reduce(r);
}

// r = "xy/R mod m"; x,y != r

function montMulTo(x, y, r) {
    x.multiplyTo(y, r);
    this.reduce(r);
}

Montgomery.prototype.convert = montConvert;
Montgomery.prototype.revert = montRevert;
Montgomery.prototype.reduce = montReduce;
Montgomery.prototype.mulTo = montMulTo;
Montgomery.prototype.sqrTo = montSqrTo;

// (protected) true iff this is even

function bnpIsEven() {
    return ((this.t > 0) ? (this[0] & 1) : this.s) == 0;
}

// (protected) this^e, e < 2^32, doing sqr and mul with "r" (HAC 14.79)

function bnpExp(e, z) {
    if (e > 0xffffffff || e < 1) return BigInteger.ONE;
    let r = nbi();

    let r2 = nbi();

    let g = z.convert(this);

    let i = nbits(e) - 1;
    g.copyTo(r);
    while (--i >= 0) {
        z.sqrTo(r, r2);
        if ((e & (1 << i)) > 0) z.mulTo(r2, g, r);
        else {
            let t = r;
            r = r2;
            r2 = t;
        }
    }
    return z.revert(r);
}

// (public) this^e % m, 0 <= e < 2^32

function bnModPowInt(e, m) {
    let z;
    if (e < 256 || m.isEven()) z = new Classic(m);
    else z = new Montgomery(m);
    return this.exp(e, z);
}

// protected
BigInteger.prototype.copyTo = bnpCopyTo;
BigInteger.prototype.fromInt = bnpFromInt;
BigInteger.prototype.fromString = bnpFromString;
BigInteger.prototype.clamp = bnpClamp;
BigInteger.prototype.dlShiftTo = bnpDLShiftTo;
BigInteger.prototype.drShiftTo = bnpDRShiftTo;
BigInteger.prototype.lShiftTo = bnpLShiftTo;
BigInteger.prototype.rShiftTo = bnpRShiftTo;
BigInteger.prototype.subTo = bnpSubTo;
BigInteger.prototype.multiplyTo = bnpMultiplyTo;
BigInteger.prototype.squareTo = bnpSquareTo;
BigInteger.prototype.divRemTo = bnpDivRemTo;
BigInteger.prototype.invDigit = bnpInvDigit;
BigInteger.prototype.isEven = bnpIsEven;
BigInteger.prototype.exp = bnpExp;

// public
BigInteger.prototype.toString = bnToString;
BigInteger.prototype.negate = bnNegate;
BigInteger.prototype.abs = bnAbs;
BigInteger.prototype.compareTo = bnCompareTo;
BigInteger.prototype.bitLength = bnBitLength;
BigInteger.prototype.mod = bnMod;
BigInteger.prototype.modPowInt = bnModPowInt;

// "constants"
BigInteger.ZERO = nbv(0);
BigInteger.ONE = nbv(1);

function bnClone() {
    let r = nbi();
    this.copyTo(r);
    return r;
}

// (public) return value as integer

function bnIntValue() {
    if (this.s < 0) {
        if (this.t == 1) return this[0] - this.DV;
        else if (this.t == 0) return -1;
    } else if (this.t == 1) return this[0];
    else if (this.t == 0) return 0;
    // assumes 16 < DB < 32
    return ((this[1] & ((1 << (32 - this.DB)) - 1)) << this.DB) | this[0];
}

// (public) return value as byte

function bnByteValue() {
    return (this.t == 0) ? this.s : (this[0] << 24) >> 24;
}

// (public) return value as short (assumes DB>=16)

function bnShortValue() {
    return (this.t == 0) ? this.s : (this[0] << 16) >> 16;
}

// (protected) return x s.t. r^x < DV

function bnpChunkSize(r) {
    return Math.floor(Math.LN2 * this.DB / Math.log(r));
}

// (public) 0 if this == 0, 1 if this > 0

function bnSigNum() {
    if (this.s < 0) return -1;
    else if (this.t <= 0 || (this.t == 1 && this[0] <= 0)) return 0;
    else return 1;
}

// (protected) convert to radix string

function bnpToRadix(b) {
    if (b == null) b = 10;
    if (this.signum() == 0 || b < 2 || b > 36) return '0';
    let cs = this.chunkSize(b);
    let a = Math.pow(b, cs);
    let d = nbv(a);

    let y = nbi();

    let z = nbi();

    let r = '';
    this.divRemTo(d, y, z);
    while (y.signum() > 0) {
        r = (a + z.intValue()).toString(b).substr(1) + r;
        y.divRemTo(d, y, z);
    }
    return z.intValue().toString(b) + r;
}

// (protected) convert from radix string

function bnpFromRadix(s, b) {
    this.fromInt(0);
    if (b == null) b = 10;
    let cs = this.chunkSize(b);
    let d = Math.pow(b, cs);

    let mi = false;

    let j = 0;

    let w = 0;
    for (let i = 0; i < s.length; ++i) {
        let x = intAt(s, i);
        if (x < 0) {
            if (s.charAt(i) == '-' && this.signum() == 0) mi = true;
            continue;
        }
        w = b * w + x;
        if (++j >= cs) {
            this.dMultiply(d);
            this.dAddOffset(w, 0);
            j = 0;
            w = 0;
        }
    }
    if (j > 0) {
        this.dMultiply(Math.pow(b, j));
        this.dAddOffset(w, 0);
    }
    if (mi) BigInteger.ZERO.subTo(this, this);
}

// (protected) alternate constructor

function bnpFromNumber(a, b, c) {
    if (typeof b == 'number') {
        // new BigInteger(int,int,RNG)
        if (a < 2) this.fromInt(1);
        else {
            this.fromNumber(a, c);
            if (!this.testBit(a - 1)) // force MSB set
            {
                this.bitwiseTo(BigInteger.ONE.shiftLeft(a - 1), op_or, this);
            }
            if (this.isEven()) this.dAddOffset(1, 0); // force odd
            while (!this.isProbablePrime(b)) {
                this.dAddOffset(2, 0);
                if (this.bitLength() > a) this.subTo(BigInteger.ONE.shiftLeft(a - 1), this);
            }
        }
    } else {
        // new BigInteger(int,RNG)
        let x = new Array();

        let t = a & 7;
        x.length = (a >> 3) + 1;
        b.nextBytes(x);
        if (t > 0) x[0] &= ((1 << t) - 1);
        else x[0] = 0;
        this.fromString(x, 256);
    }
}

// (public) convert to bigendian byte array

function bnToByteArray() {
    let i = this.t;

    let r = new Array();
    r[0] = this.s;
    let p = this.DB - (i * this.DB) % 8;

    let d; let k = 0;
    if (i-- > 0) {
        if (p < this.DB && (d = this[i] >> p) != (this.s & this.DM) >> p) r[k++] = d | (this.s << (this.DB - p));
        while (i >= 0) {
            if (p < 8) {
                d = (this[i] & ((1 << p) - 1)) << (8 - p);
                d |= this[--i] >> (p += this.DB - 8);
            } else {
                d = (this[i] >> (p -= 8)) & 0xff;
                if (p <= 0) {
                    p += this.DB;
                    --i;
                }
            }
            if ((d & 0x80) != 0) d |= -256;
            if (k == 0 && (this.s & 0x80) != (d & 0x80))++k;
            if (k > 0 || d != this.s) r[k++] = d;
        }
    }
    return r;
}

function bnEquals(a) {
    return (this.compareTo(a) == 0);
}

function bnMin(a) {
    return (this.compareTo(a) < 0) ? this : a;
}

function bnMax(a) {
    return (this.compareTo(a) > 0) ? this : a;
}

// (protected) r = this op a (bitwise)

function bnpBitwiseTo(a, op, r) {
    let i; let f; let m = Math.min(a.t, this.t);
    for (i = 0; i < m; ++i) r[i] = op(this[i], a[i]);
    if (a.t < this.t) {
        f = a.s & this.DM;
        for (i = m; i < this.t; ++i) r[i] = op(this[i], f);
        r.t = this.t;
    } else {
        f = this.s & this.DM;
        for (i = m; i < a.t; ++i) r[i] = op(f, a[i]);
        r.t = a.t;
    }
    r.s = op(this.s, a.s);
    r.clamp();
}

// (public) this & a

function op_and(x, y) {
    return x & y;
}

function bnAnd(a) {
    let r = nbi();
    this.bitwiseTo(a, op_and, r);
    return r;
}

// (public) this | a

function op_or(x, y) {
    return x | y;
}

function bnOr(a) {
    let r = nbi();
    this.bitwiseTo(a, op_or, r);
    return r;
}

// (public) this ^ a

function op_xor(x, y) {
    return x ^ y;
}

function bnXor(a) {
    let r = nbi();
    this.bitwiseTo(a, op_xor, r);
    return r;
}

// (public) this & ~a

function op_andnot(x, y) {
    return x & ~y;
}

function bnAndNot(a) {
    let r = nbi();
    this.bitwiseTo(a, op_andnot, r);
    return r;
}

// (public) ~this

function bnNot() {
    let r = nbi();
    for (let i = 0; i < this.t; ++i) r[i] = this.DM & ~this[i];
    r.t = this.t;
    r.s = ~this.s;
    return r;
}

// (public) this << n

function bnShiftLeft(n) {
    let r = nbi();
    if (n < 0) this.rShiftTo(-n, r);
    else this.lShiftTo(n, r);
    return r;
}

// (public) this >> n

function bnShiftRight(n) {
    let r = nbi();
    if (n < 0) this.lShiftTo(-n, r);
    else this.rShiftTo(n, r);
    return r;
}

// return index of lowest 1-bit in x, x < 2^31

function lbit(x) {
    if (x == 0) return -1;
    let r = 0;
    if ((x & 0xffff) == 0) {
        x >>= 16;
        r += 16;
    }
    if ((x & 0xff) == 0) {
        x >>= 8;
        r += 8;
    }
    if ((x & 0xf) == 0) {
        x >>= 4;
        r += 4;
    }
    if ((x & 3) == 0) {
        x >>= 2;
        r += 2;
    }
    if ((x & 1) == 0)++r;
    return r;
}

// (public) returns index of lowest 1-bit (or -1 if none)

function bnGetLowestSetBit() {
    for (let i = 0; i < this.t; ++i) {
        if (this[i] != 0) return i * this.DB + lbit(this[i]);
    }
    if (this.s < 0) return this.t * this.DB;
    return -1;
}

// return number of 1 bits in x

function cbit(x) {
    let r = 0;
    while (x != 0) {
        x &= x - 1;
        ++r;
    }
    return r;
}

// (public) return number of set bits

function bnBitCount() {
    let r = 0;

    let x = this.s & this.DM;
    for (let i = 0; i < this.t; ++i) r += cbit(this[i] ^ x);
    return r;
}

// (public) true iff nth bit is set

function bnTestBit(n) {
    let j = Math.floor(n / this.DB);
    if (j >= this.t) return (this.s != 0);
    return ((this[j] & (1 << (n % this.DB))) != 0);
}

// (protected) this op (1<<n)

function bnpChangeBit(n, op) {
    let r = BigInteger.ONE.shiftLeft(n);
    this.bitwiseTo(r, op, r);
    return r;
}

// (public) this | (1<<n)

function bnSetBit(n) {
    return this.changeBit(n, op_or);
}

// (public) this & ~(1<<n)

function bnClearBit(n) {
    return this.changeBit(n, op_andnot);
}

// (public) this ^ (1<<n)

function bnFlipBit(n) {
    return this.changeBit(n, op_xor);
}

// (protected) r = this + a

function bnpAddTo(a, r) {
    let i = 0;

    let c = 0;

    let m = Math.min(a.t, this.t);
    while (i < m) {
        c += this[i] + a[i];
        r[i++] = c & this.DM;
        c >>= this.DB;
    }
    if (a.t < this.t) {
        c += a.s;
        while (i < this.t) {
            c += this[i];
            r[i++] = c & this.DM;
            c >>= this.DB;
        }
        c += this.s;
    } else {
        c += this.s;
        while (i < a.t) {
            c += a[i];
            r[i++] = c & this.DM;
            c >>= this.DB;
        }
        c += a.s;
    }
    r.s = (c < 0) ? -1 : 0;
    if (c > 0) r[i++] = c;
    else if (c < -1) r[i++] = this.DV + c;
    r.t = i;
    r.clamp();
}

// (public) this + a

function bnAdd(a) {
    let r = nbi();
    this.addTo(a, r);
    return r;
}

// (public) this - a

function bnSubtract(a) {
    let r = nbi();
    this.subTo(a, r);
    return r;
}

// (public) this * a

function bnMultiply(a) {
    let r = nbi();
    this.multiplyTo(a, r);
    return r;
}

// (public) this^2

function bnSquare() {
    let r = nbi();
    this.squareTo(r);
    return r;
}

// (public) this / a

function bnDivide(a) {
    let r = nbi();
    this.divRemTo(a, r, null);
    return r;
}

// (public) this % a

function bnRemainder(a) {
    let r = nbi();
    this.divRemTo(a, null, r);
    return r;
}

// (public) [this/a,this%a]

function bnDivideAndRemainder(a) {
    let q = nbi();

    let r = nbi();
    this.divRemTo(a, q, r);
    return new Array(q, r);
}

// (protected) this *= n, this >= 0, 1 < n < DV

function bnpDMultiply(n) {
    this[this.t] = this.am(0, n - 1, this, 0, 0, this.t);
    ++this.t;
    this.clamp();
}

// (protected) this += n << w words, this >= 0

function bnpDAddOffset(n, w) {
    if (n == 0) return;
    while (this.t <= w) this[this.t++] = 0;
    this[w] += n;
    while (this[w] >= this.DV) {
        this[w] -= this.DV;
        if (++w >= this.t) this[this.t++] = 0;
        ++this[w];
    }
}

// A "null" reducer

function NullExp() {}

function nNop(x) {
    return x;
}

function nMulTo(x, y, r) {
    x.multiplyTo(y, r);
}

function nSqrTo(x, r) {
    x.squareTo(r);
}

NullExp.prototype.convert = nNop;
NullExp.prototype.revert = nNop;
NullExp.prototype.mulTo = nMulTo;
NullExp.prototype.sqrTo = nSqrTo;

// (public) this^e

function bnPow(e) {
    return this.exp(e, new NullExp());
}

// (protected) r = lower n words of "this * a", a.t <= n
// "this" should be the larger one if appropriate.

function bnpMultiplyLowerTo(a, n, r) {
    let i = Math.min(this.t + a.t, n);
    r.s = 0; // assumes a,this >= 0
    r.t = i;
    while (i > 0) r[--i] = 0;
    let j;
    for (j = r.t - this.t; i < j; ++i) r[i + this.t] = this.am(0, a[i], r, i, 0, this.t);
    for (j = Math.min(a.t, n); i < j; ++i) this.am(0, a[i], r, i, 0, n - i);
    r.clamp();
}

// (protected) r = "this * a" without lower n words, n > 0
// "this" should be the larger one if appropriate.

function bnpMultiplyUpperTo(a, n, r) {
    --n;
    let i = r.t = this.t + a.t - n;
    r.s = 0; // assumes a,this >= 0
    while (--i >= 0) r[i] = 0;
    for (i = Math.max(n - this.t, 0); i < a.t; ++i) {
        r[this.t + i - n] = this.am(n - i, a[i], r, 0, 0, this.t + i - n);
    }
    r.clamp();
    r.drShiftTo(1, r);
}

// Barrett modular reduction

function Barrett(m) {
    // setup Barrett
    this.r2 = nbi();
    this.q3 = nbi();
    BigInteger.ONE.dlShiftTo(2 * m.t, this.r2);
    this.mu = this.r2.divide(m);
    this.m = m;
}

function barrettConvert(x) {
    if (x.s < 0 || x.t > 2 * this.m.t) return x.mod(this.m);
    else if (x.compareTo(this.m) < 0) return x;
    else {
        let r = nbi();
        x.copyTo(r);
        this.reduce(r);
        return r;
    }
}

function barrettRevert(x) {
    return x;
}

// x = x mod m (HAC 14.42)

function barrettReduce(x) {
    x.drShiftTo(this.m.t - 1, this.r2);
    if (x.t > this.m.t + 1) {
        x.t = this.m.t + 1;
        x.clamp();
    }
    this.mu.multiplyUpperTo(this.r2, this.m.t + 1, this.q3);
    this.m.multiplyLowerTo(this.q3, this.m.t + 1, this.r2);
    while (x.compareTo(this.r2) < 0) x.dAddOffset(1, this.m.t + 1);
    x.subTo(this.r2, x);
    while (x.compareTo(this.m) >= 0) x.subTo(this.m, x);
}

// r = x^2 mod m; x != r

function barrettSqrTo(x, r) {
    x.squareTo(r);
    this.reduce(r);
}

// r = x*y mod m; x,y != r

function barrettMulTo(x, y, r) {
    x.multiplyTo(y, r);
    this.reduce(r);
}

Barrett.prototype.convert = barrettConvert;
Barrett.prototype.revert = barrettRevert;
Barrett.prototype.reduce = barrettReduce;
Barrett.prototype.mulTo = barrettMulTo;
Barrett.prototype.sqrTo = barrettSqrTo;

// (public) this^e % m (HAC 14.85)

function bnModPow(e, m) {
    let i = e.bitLength();

    let k; let r = nbv(1);

    let z;
    if (i <= 0) return r;
    else if (i < 18) k = 1;
    else if (i < 48) k = 3;
    else if (i < 144) k = 4;
    else if (i < 768) k = 5;
    else k = 6;
    if (i < 8) z = new Classic(m);
    else if (m.isEven()) z = new Barrett(m);
    else z = new Montgomery(m);

    // precomputation
    let g = new Array();

    let n = 3;

    let k1 = k - 1;

    let km = (1 << k) - 1;
    g[1] = z.convert(this);
    if (k > 1) {
        let g2 = nbi();
        z.sqrTo(g[1], g2);
        while (n <= km) {
            g[n] = nbi();
            z.mulTo(g2, g[n - 2], g[n]);
            n += 2;
        }
    }

    let j = e.t - 1;

    let w; let is1 = true;

    let r2 = nbi();

    let t;
    i = nbits(e[j]) - 1;
    while (j >= 0) {
        if (i >= k1) w = (e[j] >> (i - k1)) & km;
        else {
            w = (e[j] & ((1 << (i + 1)) - 1)) << (k1 - i);
            if (j > 0) w |= e[j - 1] >> (this.DB + i - k1);
        }

        n = k;
        while ((w & 1) == 0) {
            w >>= 1;
            --n;
        }
        if ((i -= n) < 0) {
            i += this.DB;
            --j;
        }
        if (is1) { // ret == 1, don't bother squaring or multiplying it
            g[w].copyTo(r);
            is1 = false;
        } else {
            while (n > 1) {
                z.sqrTo(r, r2);
                z.sqrTo(r2, r);
                n -= 2;
            }
            if (n > 0) z.sqrTo(r, r2);
            else {
                t = r;
                r = r2;
                r2 = t;
            }
            z.mulTo(r2, g[w], r);
        }

        while (j >= 0 && (e[j] & (1 << i)) == 0) {
            z.sqrTo(r, r2);
            t = r;
            r = r2;
            r2 = t;
            if (--i < 0) {
                i = this.DB - 1;
                --j;
            }
        }
    }
    return z.revert(r);
}

// (public) gcd(this,a) (HAC 14.54)

function bnGCD(a) {
    let x = (this.s < 0) ? this.negate() : this.clone();
    let y = (a.s < 0) ? a.negate() : a.clone();
    if (x.compareTo(y) < 0) {
        let t = x;
        x = y;
        y = t;
    }
    let i = x.getLowestSetBit();

    let g = y.getLowestSetBit();
    if (g < 0) return x;
    if (i < g) g = i;
    if (g > 0) {
        x.rShiftTo(g, x);
        y.rShiftTo(g, y);
    }
    while (x.signum() > 0) {
        if ((i = x.getLowestSetBit()) > 0) x.rShiftTo(i, x);
        if ((i = y.getLowestSetBit()) > 0) y.rShiftTo(i, y);
        if (x.compareTo(y) >= 0) {
            x.subTo(y, x);
            x.rShiftTo(1, x);
        } else {
            y.subTo(x, y);
            y.rShiftTo(1, y);
        }
    }
    if (g > 0) y.lShiftTo(g, y);
    return y;
}

// (protected) this % n, n < 2^26

function bnpModInt(n) {
    if (n <= 0) return 0;
    let d = this.DV % n;

    let r = (this.s < 0) ? n - 1 : 0;
    if (this.t > 0) {
        if (d == 0) r = this[0] % n;
        else for (let i = this.t - 1; i >= 0; --i) r = (d * r + this[i]) % n;
    }
    return r;
}

// (public) 1/this % m (HAC 14.61)

function bnModInverse(m) {
    let ac = m.isEven();
    if ((this.isEven() && ac) || m.signum() == 0) return BigInteger.ZERO;
    let u = m.clone();

    let v = this.clone();
    let a = nbv(1);

    let b = nbv(0);

    let c = nbv(0);

    let d = nbv(1);
    while (u.signum() != 0) {
        while (u.isEven()) {
            u.rShiftTo(1, u);
            if (ac) {
                if (!a.isEven() || !b.isEven()) {
                    a.addTo(this, a);
                    b.subTo(m, b);
                }
                a.rShiftTo(1, a);
            } else if (!b.isEven()) b.subTo(m, b);
            b.rShiftTo(1, b);
        }
        while (v.isEven()) {
            v.rShiftTo(1, v);
            if (ac) {
                if (!c.isEven() || !d.isEven()) {
                    c.addTo(this, c);
                    d.subTo(m, d);
                }
                c.rShiftTo(1, c);
            } else if (!d.isEven()) d.subTo(m, d);
            d.rShiftTo(1, d);
        }
        if (u.compareTo(v) >= 0) {
            u.subTo(v, u);
            if (ac) a.subTo(c, a);
            b.subTo(d, b);
        } else {
            v.subTo(u, v);
            if (ac) c.subTo(a, c);
            d.subTo(b, d);
        }
    }
    if (v.compareTo(BigInteger.ONE) != 0) return BigInteger.ZERO;
    if (d.compareTo(m) >= 0) return d.subtract(m);
    if (d.signum() < 0) d.addTo(m, d);
    else return d;
    if (d.signum() < 0) return d.add(m);
    else return d;
}

let lowprimes = [2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541, 547, 557, 563, 569, 571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911, 919, 929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997];
let lplim = (1 << 26) / lowprimes[lowprimes.length - 1];

// (public) test primality with certainty >= 1-.5^t

function bnIsProbablePrime(t) {
    let i; let x = this.abs();
    if (x.t == 1 && x[0] <= lowprimes[lowprimes.length - 1]) {
        for (i = 0; i < lowprimes.length; ++i) {
            if (x[0] == lowprimes[i]) return true;
        }
        return false;
    }
    if (x.isEven()) return false;
    i = 1;
    while (i < lowprimes.length) {
        let m = lowprimes[i];

        let j = i + 1;
        while (j < lowprimes.length && m < lplim) m *= lowprimes[j++];
        m = x.modInt(m);
        while (i < j) if (m % lowprimes[i++] == 0) return false;
    }
    return x.millerRabin(t);
}

// (protected) true if probably prime (HAC 4.24, Miller-Rabin)

function bnpMillerRabin(t) {
    let n1 = this.subtract(BigInteger.ONE);
    let k = n1.getLowestSetBit();
    if (k <= 0) return false;
    let r = n1.shiftRight(k);
    t = (t + 1) >> 1;
    if (t > lowprimes.length) t = lowprimes.length;
    let a = nbi();
    for (let i = 0; i < t; ++i) {
        // Pick bases at random, instead of starting at 2
        a.fromInt(lowprimes[Math.floor(Math.random() * lowprimes.length)]);
        let y = a.modPow(r, this);
        if (y.compareTo(BigInteger.ONE) != 0 && y.compareTo(n1) != 0) {
            let j = 1;
            while (j++ < k && y.compareTo(n1) != 0) {
                y = y.modPowInt(2, this);
                if (y.compareTo(BigInteger.ONE) == 0) return false;
            }
            if (y.compareTo(n1) != 0) return false;
        }
    }
    return true;
}

// protected
BigInteger.prototype.chunkSize = bnpChunkSize;
BigInteger.prototype.toRadix = bnpToRadix;
BigInteger.prototype.fromRadix = bnpFromRadix;
BigInteger.prototype.fromNumber = bnpFromNumber;
BigInteger.prototype.bitwiseTo = bnpBitwiseTo;
BigInteger.prototype.changeBit = bnpChangeBit;
BigInteger.prototype.addTo = bnpAddTo;
BigInteger.prototype.dMultiply = bnpDMultiply;
BigInteger.prototype.dAddOffset = bnpDAddOffset;
BigInteger.prototype.multiplyLowerTo = bnpMultiplyLowerTo;
BigInteger.prototype.multiplyUpperTo = bnpMultiplyUpperTo;
BigInteger.prototype.modInt = bnpModInt;
BigInteger.prototype.millerRabin = bnpMillerRabin;

// public
BigInteger.prototype.clone = bnClone;
BigInteger.prototype.intValue = bnIntValue;
BigInteger.prototype.byteValue = bnByteValue;
BigInteger.prototype.shortValue = bnShortValue;
BigInteger.prototype.signum = bnSigNum;
BigInteger.prototype.toByteArray = bnToByteArray;
BigInteger.prototype.equals = bnEquals;
BigInteger.prototype.min = bnMin;
BigInteger.prototype.max = bnMax;
BigInteger.prototype.and = bnAnd;
BigInteger.prototype.or = bnOr;
BigInteger.prototype.xor = bnXor;
BigInteger.prototype.andNot = bnAndNot;
BigInteger.prototype.not = bnNot;
BigInteger.prototype.shiftLeft = bnShiftLeft;
BigInteger.prototype.shiftRight = bnShiftRight;
BigInteger.prototype.getLowestSetBit = bnGetLowestSetBit;
BigInteger.prototype.bitCount = bnBitCount;
BigInteger.prototype.testBit = bnTestBit;
BigInteger.prototype.setBit = bnSetBit;
BigInteger.prototype.clearBit = bnClearBit;
BigInteger.prototype.flipBit = bnFlipBit;
BigInteger.prototype.add = bnAdd;
BigInteger.prototype.subtract = bnSubtract;
BigInteger.prototype.multiply = bnMultiply;
BigInteger.prototype.divide = bnDivide;
BigInteger.prototype.remainder = bnRemainder;
BigInteger.prototype.divideAndRemainder = bnDivideAndRemainder;
BigInteger.prototype.modPow = bnModPow;
BigInteger.prototype.modInverse = bnModInverse;
BigInteger.prototype.pow = bnPow;
BigInteger.prototype.gcd = bnGCD;
BigInteger.prototype.isProbablePrime = bnIsProbablePrime;

// JSBN-specific extension
BigInteger.prototype.square = bnSquare;

let RSAPublicKey = function($modulus, $encryptionExponent) {
    this.modulus = new BigInteger(Hex.encode($modulus), 16);
    this.encryptionExponent = new BigInteger(Hex.encode($encryptionExponent), 16);
};

let UTF8 = {
    encode: function($input) {
        $input = $input.replace(/\r\n/g, '\n');
        let $output = '';
        for (let $n = 0; $n < $input.length; $n++) {
            let $c = $input.charCodeAt($n);
            if ($c < 128) {
                $output += String.fromCharCode($c);
            } else if (($c > 127) && ($c < 2048)) {
                $output += String.fromCharCode(($c >> 6) | 192);
                $output += String.fromCharCode(($c & 63) | 128);
            } else {
                $output += String.fromCharCode(($c >> 12) | 224);
                $output += String.fromCharCode((($c >> 6) & 63) | 128);
                $output += String.fromCharCode(($c & 63) | 128);
            }
        }
        return $output;
    },
    decode: function($input) {
        let $output = '';
        let $i = 0;
        let $c = $c1 = $c2 = 0;
        while ($i < $input.length) {
            $c = $input.charCodeAt($i);
            if ($c < 128) {
                $output += String.fromCharCode($c);
                $i++;
            } else if (($c > 191) && ($c < 224)) {
                $c2 = $input.charCodeAt($i + 1);
                $output += String.fromCharCode((($c & 31) << 6) | ($c2 & 63));
                $i += 2;
            } else {
                $c2 = $input.charCodeAt($i + 1);
                $c3 = $input.charCodeAt($i + 2);
                $output += String.fromCharCode((($c & 15) << 12) | (($c2 & 63) << 6) | ($c3 & 63));
                $i += 3;
            }
        }
        return $output;
    },
};

let Base64 = {
    base64: 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=',
    encode: function($input) {
        if (!$input) {
            return false;
        }
        // default comment
        // $input = UTF8.encode($input);
        let $output = '';
        let $chr1; let $chr2; let $chr3;
        let $enc1; let $enc2; let $enc3; let $enc4;
        let $i = 0;
        do {
            $chr1 = $input.charCodeAt($i++);
            $chr2 = $input.charCodeAt($i++);
            $chr3 = $input.charCodeAt($i++);
            $enc1 = $chr1 >> 2;
            $enc2 = (($chr1 & 3) << 4) | ($chr2 >> 4);
            $enc3 = (($chr2 & 15) << 2) | ($chr3 >> 6);
            $enc4 = $chr3 & 63;
            if (isNaN($chr2)) $enc3 = $enc4 = 64;
            else if (isNaN($chr3)) $enc4 = 64;
            $output += this.base64.charAt($enc1) + this.base64.charAt($enc2) + this.base64.charAt($enc3) + this.base64.charAt($enc4);
        } while ($i < $input.length);
        return $output;
    },
    decode: function($input) {
        if (!$input) return false;
        $input = $input.replace(/[^A-Za-z0-9\+\/\=]/g, '');
        let $output = '';
        let $enc1; let $enc2; let $enc3; let $enc4;
        let $i = 0;
        do {
            $enc1 = this.base64.indexOf($input.charAt($i++));
            $enc2 = this.base64.indexOf($input.charAt($i++));
            $enc3 = this.base64.indexOf($input.charAt($i++));
            $enc4 = this.base64.indexOf($input.charAt($i++));
            $output += String.fromCharCode(($enc1 << 2) | ($enc2 >> 4));
            if ($enc3 != 64) $output += String.fromCharCode((($enc2 & 15) << 4) | ($enc3 >> 2));
            if ($enc4 != 64) $output += String.fromCharCode((($enc3 & 3) << 6) | $enc4);
        } while ($i < $input.length);
        return $output; // UTF8.decode($output);
    },
};

var Hex = {
    hex: '0123456789abcdef',
    encode: function($input) {
        if (!$input) return false;
        let $output = '';
        let $k;
        let $i = 0;
        do {
            $k = $input.charCodeAt($i++);
            $output += this.hex.charAt(($k >> 4) & 0xf) + this.hex.charAt($k & 0xf);
        } while ($i < $input.length);
        return $output;
    },
    decode: function($input) {
        if (!$input) return false;
        $input = $input.replace(/[^0-9abcdef]/g, '');
        let $output = '';
        let $i = 0;
        do {
            $output += String.fromCharCode(((this.hex.indexOf($input.charAt($i++)) << 4) & 0xf0) | (this.hex.indexOf($input.charAt($i++)) & 0xf));
        } while ($i < $input.length);
        return $output;
    },
};

let ASN1Data = function($data) {
    this.error = false;
    this.parse = function($data) {
        if (!$data) {
            this.error = true;
            return null;
        }
        let $result = [];
        while ($data.length > 0) {
            // get the tag
            let $tag = $data.charCodeAt(0);
            $data = $data.substr(1);
            // get length
            let $length = 0;
            // ignore any null tag
            if (($tag & 31) == 0x5) $data = $data.substr(1);
            else {
                if ($data.charCodeAt(0) & 128) {
                    let $lengthSize = $data.charCodeAt(0) & 127;
                    $data = $data.substr(1);
                    if ($lengthSize > 0) $length = $data.charCodeAt(0);
                    if ($lengthSize > 1) $length = (($length << 8) | $data.charCodeAt(1));
                    if ($lengthSize > 2) {
                        this.error = true;
                        return null;
                    }
                    $data = $data.substr($lengthSize);
                } else {
                    $length = $data.charCodeAt(0);
                    $data = $data.substr(1);
                }
            }
            // get value
            let $value = '';
            if ($length) {
                if ($length > $data.length) {
                    this.error = true;
                    return null;
                }
                $value = $data.substr(0, $length);
                $data = $data.substr($length);
            }
            if ($tag & 32) {
                $result.push(this.parse($value));
            } // sequence
            else {
                $result.push(this.value(($tag & 128) ? 4 : ($tag & 31), $value));
            }
        }
        return $result;
    };
    this.value = function($tag, $data) {
        if ($tag == 1) {
            return !!$data;
        } else if ($tag == 2) // integer
        {
            return $data;
        } else if ($tag == 3) // bit string
        {
            return this.parse($data.substr(1));
        } else if ($tag == 5) // null
        {
            return null;
        } else if ($tag == 6) { // ID
            let $res = [];
            let $d0 = $data.charCodeAt(0);
            $res.push(Math.floor($d0 / 40));
            $res.push($d0 - $res[0] * 40);
            let $stack = [];
            let $powNum = 0;
            let $i;
            for ($i = 1; $i < $data.length; $i++) {
                let $token = $data.charCodeAt($i);
                $stack.push($token & 127);
                if ($token & 128) {
                    $powNum++;
                } else {
                    var $j;
                    let $sum = 0;
                    for ($j = 0; $j < $stack.length; $j++) {
                        $sum += $stack[$j] * Math.pow(128, $powNum--);
                    }
                    $res.push($sum);
                    $powNum = 0;
                    $stack = [];
                }
            }
            return $res.join('.');
        }
        return null;
    };
    this.data = this.parse($data);
};

var RSA = {
    getPublicKey: function($pem) {
        if ($pem.length < 50) return false;
        if ($pem.substr(0, 26) != '-----BEGIN PUBLIC KEY-----') return false;
        $pem = $pem.substr(26);
        if ($pem.substr($pem.length - 24) != '-----END PUBLIC KEY-----') return false;
        $pem = $pem.substr(0, $pem.length - 24);
        $pem = new ASN1Data(Base64.decode($pem));
        if ($pem.error) return false;
        $pem = $pem.data;
        if ($pem[0][0][0] == '1.2.840.113549.1.1.1') {
            return new RSAPublicKey($pem[0][1][0][0], $pem[0][1][0][1]);
        }
        return false;
    },
    encrypt: function($data, $pubkey) {
        // encode to utf-8
        $data = UTF8.encode($data);
        if (!$pubkey) return false;
        let bytes = ($pubkey.modulus.bitLength() + 7) >> 3;
        $data = this.pkcs1pad2($data, bytes);
        if (!$data) return false;
        $data = $data.modPowInt($pubkey.encryptionExponent, $pubkey.modulus);
        if (!$data) return false;
        $data = $data.toString(16);
        while ($data.length < bytes * 2) {
            $data = '0' + $data;
        }
        // return $data;
        return Base64.encode(Hex.decode($data));
    },
    pkcs1pad2: function($data, $keysize) {
        if ($keysize < $data.length + 11) {
            return null;
        }
        let $buffer = [];
        let $i = $data.length - 1;
        while ($i >= 0 && $keysize > 0) {
            $buffer[--$keysize] = $data.charCodeAt($i--);
        }
        $buffer[--$keysize] = 0;
        while ($keysize > 2) {
            $buffer[--$keysize] = Math.floor(Math.random() * 254) + 1;
        }
        $buffer[--$keysize] = 2;
        $buffer[--$keysize] = 0;
        return new BigInteger($buffer);
    },
};

export var RSA = RSA;
