"use strict";
/**
 * --------------------------------------------------------------------
 * Copyright 2015 Nikolay Mavrenkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --------------------------------------------------------------------
 *
 * Author:  Nikolay Mavrenkov <koluch@koluch.ru>
 * Created: 16.11.2015 02:14
 */
var fs = require("fs");

var data = JSON.parse(fs.readFileSync("data.json"));

/*
    Basic table-data manipulation functions
 */
function for_group(data, f) {
    if(data.constructor === Array) {
        return f(data.slice());
    }
    else {
        var result = {};
        for(var i in data) {
            result[i] = for_group(data[i], f);
        }
        return result;
    }
}

function tmap(data, f) {
    return for_group(data, (group) => group.map(f));
}

/*
    Special functions to handle data - sorting, grouping and so on
 */
function group(ar) {
    var fs = Array.prototype.slice.call(arguments, 1);
    var f = fs[0];
    if(f) {
        var result = {};
        for (var i = 0; i < ar.length; i++) {
            var el = ar[i];
            var cat = f(el);
            if (!result[cat]) {
                result[cat] = [];
            }
            result[cat].push(el);
        }
        var restFs = fs.slice(1);
        for(var i in result) {
            result[i] = group.apply(null, [result[i]].concat(restFs));
        }
        return result;
    }
    else {
        return ar;
    }
}

function sort(data) {
    var fs = Array.prototype.slice.call(arguments, 1);
    return for_group(data, (group) => (
        group.sort((x,y) => {
            var result = 0;
            for (var i = 0; i < fs.length; i++) {
                var f = fs[i];
                result = f(x,y);
                if(result!=0) break;
            }
            return result;
        })
    ));
}

function hideFields(data, fields) {
    return tmap(data, (row) => {
        var result = {};
        for(var i in row) {
            if(fields.indexOf(i)==-1) {
                result[i] = row[i];
            }
        }
        return result;
    });
}


/*
    aux
 */
function comp(v1, v2) {
    var asNumber1 = new Number(v1);
    var asNumber2 = new Number(v2);
    if(!isNaN(asNumber1) && !isNaN(asNumber2)) {
        return asNumber1 - asNumber2;
    }
    else {
        return v1.localeCompare(v2);
    }
}

function printColumn(str, length) {
    var s = str;
    for(var i = str.length; i<length; ++i) {
        s+=" ";
    }
    return s;
}

function print(data, indent) {
    var colWidth = 25;

    indent = indent || 0;
    if(data.constructor === Array) {
        var firstRow = data[0];
        if(firstRow) {
            var str = "";
            for(var k = 0; k<indent; k++) {str += "    ";}
            for(var i in firstRow) {
                str += printColumn("~" + i + "~", colWidth);
            }
            console.log(str);
        }
        for (var i = 0; i < data.length; i++) {
            var str = "";
            for(var k = 0; k<indent; k++) {str += "    ";}
            var row = data[i];
            for(var j in row) {
                str += printColumn(row[j], colWidth);
            }
            console.log(str);
        }
    }
    else {
        for(var i in data) {
            var str = "";
            for(var k = 0; k<indent; k++) {str += "    ";}
            console.log(str + i + ":");
            print(data[i], indent + 1)
        }
    }
}

/*
    convenient helpers
 */
function show(data, groupBy, sortBy, hide) {
    var groupFs = groupBy.map(group => (row => row[group]));
    var sortF = sortBy.map(field => ((row1, row2) => comp(row1[field], row2[field])));

    var grouped = group.apply(null, [data].concat(groupFs));
    var sorted = sort.apply(null, [grouped].concat(sortF));
    var hiddenColumns = hideFields(sorted, hide);
    print(hiddenColumns);
}

/*
    parse arguments
 */
var args = {};
process.argv.slice(2).forEach(arg => {
    if(!/^--.+=.+$/.test(arg)) throw new Error("Bad arg format: " + arg);
    var parts = arg.split("=");
    args[parts[0].replace(/^--/, "")] = parts[1];
});

var groupBy = args.group === undefined ? [] : args.group.split(",");
var sortBy = args.sort === undefined ? [] : args.sort.split(",");
var hide = args.hide === undefined ? [] : args.hide.split(",");

// example: > node show_data.js --groupBy=busy_factor,data_size --sortBy=score
show(data, groupBy, sortBy, hide);