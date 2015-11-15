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

for(var i = 0; i<data.length; i++) {
    var row = data[i];

    data[i] = {
        'busy_factor': row['(busyFactor)'],
        'data_size': row['(dataSize)'],
        'benchmark': row['Benchmark'],
        'cnt': row['Cnt'],
        'score': row['Score'],
        'units': row['Units']
    }

}

function group() {
    var ar = arguments[0];
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
function show(data, groups) {
    var groupFs = groups === undefined ? [] : groups.split(",").map((group) => (row) => row[group]);
    print(group.apply(null, [data].concat(groupFs)));
}

// example: "node show_data.js busy_factor,data_size"
show(data, process.argv[2]);

