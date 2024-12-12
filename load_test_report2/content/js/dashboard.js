/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 72.57870060281313, "KoPercent": 27.42129939718687};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.6626925653047555, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.76, 500, 1500, "HTTP Request-3"], "isController": false}, {"data": [1.0, 500, 1500, "HTTP Request-16"], "isController": false}, {"data": [0.76953125, 500, 1500, "HTTP Request-2"], "isController": false}, {"data": [1.0, 500, 1500, "HTTP Request-15"], "isController": false}, {"data": [0.75, 500, 1500, "HTTP Request-5"], "isController": false}, {"data": [0.875, 500, 1500, "HTTP Request-14"], "isController": false}, {"data": [0.7727272727272727, 500, 1500, "HTTP Request-4"], "isController": false}, {"data": [1.0, 500, 1500, "HTTP Request-13"], "isController": false}, {"data": [0.875, 500, 1500, "HTTP Request-19"], "isController": false}, {"data": [0.7823834196891192, 500, 1500, "HTTP Request-1"], "isController": false}, {"data": [1.0, 500, 1500, "HTTP Request-18"], "isController": false}, {"data": [1.0, 500, 1500, "HTTP Request-0"], "isController": false}, {"data": [1.0, 500, 1500, "HTTP Request-17"], "isController": false}, {"data": [1.0, 500, 1500, "HTTP Request-12"], "isController": false}, {"data": [1.0, 500, 1500, "HTTP Request-11"], "isController": false}, {"data": [1.0, 500, 1500, "HTTP Request-10"], "isController": false}, {"data": [0.0, 500, 1500, "HTTP Request-21"], "isController": false}, {"data": [0.875, 500, 1500, "HTTP Request-20"], "isController": false}, {"data": [1.0, 500, 1500, "HTTP Request-7"], "isController": false}, {"data": [0.5697885196374622, 500, 1500, "HTTP Request"], "isController": false}, {"data": [1.0, 500, 1500, "HTTP Request-6"], "isController": false}, {"data": [0.875, 500, 1500, "HTTP Request-9"], "isController": false}, {"data": [0.875, 500, 1500, "HTTP Request-8"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 7465, 2047, 27.42129939718687, 440.6743469524443, 1, 136961, 3.0, 572.0, 983.6999999999998, 3075.720000000001, 24.066127851908686, 1525.2206324078297, 4.514873537978703], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["HTTP Request-3", 200, 6, 3.0, 1295.4799999999996, 19, 120166, 316.5, 1137.3000000000002, 1795.9, 10173.7, 1.2329620062757765, 457.36682878511624, 0.15147732929641022], "isController": false}, {"data": ["HTTP Request-16", 4, 0, 0.0, 228.25, 93, 460, 180.0, 460.0, 460.0, 460.0, 0.07792258391288255, 2.507562447645764, 0.008827167708881227], "isController": false}, {"data": ["HTTP Request-2", 640, 57, 8.90625, 510.17343750000015, 12, 10086, 240.5, 979.2999999999998, 1310.0499999999988, 7882.020000000015, 5.680859940173443, 1230.9879987672978, 0.6624578234983445], "isController": false}, {"data": ["HTTP Request-15", 4, 0, 0.0, 222.5, 79, 477, 167.0, 477.0, 477.0, 477.0, 0.07800616248683645, 2.493073905963571, 0.013102597605210813], "isController": false}, {"data": ["HTTP Request-5", 8, 0, 0.0, 604.875, 85, 1347, 371.5, 1347.0, 1347.0, 1347.0, 0.11833269236458303, 25.514157859509513, 0.016813873768600423], "isController": false}, {"data": ["HTTP Request-14", 4, 0, 0.0, 327.75, 85, 893, 166.5, 893.0, 893.0, 893.0, 0.07804573480059315, 2.512268576104347, 0.008841118395379693], "isController": false}, {"data": ["HTTP Request-4", 44, 1, 2.272727272727273, 484.59090909090907, 22, 1724, 303.0, 1217.0, 1544.25, 1724.0, 0.47003023148988904, 167.9379334760338, 0.05886853241072096], "isController": false}, {"data": ["HTTP Request-13", 4, 0, 0.0, 179.0, 79, 339, 149.0, 339.0, 339.0, 339.0, 0.07809754383224647, 2.4971384571830213, 0.01311794681557265], "isController": false}, {"data": ["HTTP Request-19", 4, 0, 0.0, 361.0, 84, 1084, 138.0, 1084.0, 1084.0, 1084.0, 0.0778558498939214, 2.4889732090721526, 0.01307734978686961], "isController": false}, {"data": ["HTTP Request-1", 772, 94, 12.176165803108809, 1306.5880829015543, 5, 136957, 215.0, 712.7, 1195.5499999999988, 10088.939999999999, 3.281099253252411, 11.764990900740372, 0.36599666710230483], "isController": false}, {"data": ["HTTP Request-18", 4, 0, 0.0, 187.5, 96, 370, 142.0, 370.0, 370.0, 370.0, 0.07782858254694036, 2.505069498492071, 0.008816519116645587], "isController": false}, {"data": ["HTTP Request-0", 772, 0, 0.0, 3.9170984455958577, 2, 28, 4.0, 5.0, 6.0, 13.079999999999927, 7.610710201506369, 2.4200142207894633, 0.9290417726448204], "isController": false}, {"data": ["HTTP Request-17", 4, 0, 0.0, 204.75, 74, 426, 159.5, 426.0, 426.0, 426.0, 0.07786191189924668, 2.488596727609834, 0.013078368014326592], "isController": false}, {"data": ["HTTP Request-12", 4, 0, 0.0, 197.25, 89, 415, 142.5, 415.0, 415.0, 415.0, 0.07810364353497092, 2.5135224560178857, 0.008847678369195923], "isController": false}, {"data": ["HTTP Request-11", 4, 0, 0.0, 187.75, 80, 338, 166.5, 338.0, 338.0, 338.0, 0.07813415634644685, 2.4970119789428447, 0.013124096573817244], "isController": false}, {"data": ["HTTP Request-10", 4, 0, 0.0, 204.5, 93, 451, 137.0, 451.0, 451.0, 451.0, 0.0781921964187974, 2.5168304121217453, 0.008857709750566893], "isController": false}, {"data": ["HTTP Request-21", 4, 4, 100.0, 345.0, 91, 1013, 138.0, 1013.0, 1013.0, 1013.0, 0.07784978883244778, 2.5688339622136587, 0.008818921391175726], "isController": false}, {"data": ["HTTP Request-20", 4, 0, 0.0, 345.0, 91, 1013, 138.0, 1013.0, 1013.0, 1013.0, 0.07784978883244778, 2.5056570339717017, 0.008818921391175726], "isController": false}, {"data": ["HTTP Request-7", 4, 0, 0.0, 208.75, 79, 459, 148.5, 459.0, 459.0, 459.0, 0.07824266963988812, 2.500479847622401, 0.013142323416074957], "isController": false}, {"data": ["HTTP Request", 4965, 1885, 37.96576032225579, 332.3657603222556, 1, 136961, 3.0, 455.0, 939.7999999999993, 2269.1600000000108, 16.006473514363915, 766.3616146268218, 3.5638846989880295], "isController": false}, {"data": ["HTTP Request-6", 4, 0, 0.0, 201.5, 99, 358, 174.5, 358.0, 358.0, 358.0, 0.0781921964187974, 2.516658603096411, 0.008857709750566893], "isController": false}, {"data": ["HTTP Request-9", 4, 0, 0.0, 240.50000000000003, 80, 605, 138.5, 605.0, 605.0, 605.0, 0.0782151307170372, 2.5020439813456914, 0.013137697737627345], "isController": false}, {"data": ["HTTP Request-8", 4, 0, 0.0, 349.0, 99, 938, 179.5, 938.0, 938.0, 938.0, 0.07819372495357248, 2.5170132440621638, 0.008857882904896882], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.another.com: Nombre o servicio desconocido", 2, 0.09770395701025891, 0.02679169457468185], "isController": false}, {"data": ["Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.booking.co.uk: Nombre o servicio desconocido", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["403/Forbidden", 202, 9.86809965803615, 2.7059611520428666], "isController": false}, {"data": ["Non HTTP response code: javax.net.ssl.SSLHandshakeException/Non HTTP response message: Certificates do not conform to algorithm constraints", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["404/Not Found", 14, 0.6839276990718124, 0.18754186202277295], "isController": false}, {"data": ["429/Too Many Requests", 10, 0.48851978505129456, 0.13395847287340926], "isController": false}, {"data": ["Non HTTP response code: java.io.IOException/Non HTTP response message: Exceeded maximum number of redirects: 20", 8, 0.39081582804103565, 0.1071667782987274], "isController": false}, {"data": ["400/Bad Request", 12, 0.5862237420615535, 0.1607501674480911], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: www.animefreak.tv:80 failed to respond", 2, 0.09770395701025891, 0.02679169457468185], "isController": false}, {"data": ["503/Service Unavailable", 6, 0.29311187103077674, 0.08037508372404555], "isController": false}, {"data": ["Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.jellybean.com: Nombre o servicio desconocido", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["401/HTTP Forbidden", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["Non HTTP response code: java.lang.IllegalArgumentException/Non HTTP response message: Missing location header in redirect for GET http://www.ralphlauren.com HTTP/1.1", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.curry.com: Nombre o servicio desconocido", 2, 0.09770395701025891, 0.02679169457468185], "isController": false}, {"data": ["Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.pizza.com: Nombre o servicio desconocido", 2, 0.09770395701025891, 0.02679169457468185], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to www.babylon.com:80 [www.babylon.com/69.175.64.72] failed: Expir&oacute; el tiempo de conexi&oacute;n", 2, 0.09770395701025891, 0.02679169457468185], "isController": false}, {"data": ["Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.icanhas.cheezburger.com: Nombre o servicio desconocido", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to www.living.com:80 [www.living.com/204.78.50.82] failed: Expir&oacute; el tiempo de conexi&oacute;n", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.automotive.com: Fallo temporal en la resoluci&oacute;n del nombre", 2, 0.09770395701025891, 0.02679169457468185], "isController": false}, {"data": ["429/Too Many Requests (CDN PX)", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to www.radiology.com:80 [www.radiology.com/64.226.35.233] failed: Expir&oacute; el tiempo de conexi&oacute;n", 2, 0.09770395701025891, 0.02679169457468185], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to www.cityofnewyork.us:80 [www.cityofnewyork.us/161.185.1.156] failed: Expir&oacute; el tiempo de conexi&oacute;n", 2, 0.09770395701025891, 0.02679169457468185], "isController": false}, {"data": ["Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.booking.de: Nombre o servicio desconocido", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["406/Not Acceptable", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.tumblr.co.uk: Nombre o servicio desconocido", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["400", 70, 3.419638495359062, 0.9377093101138647], "isController": false}, {"data": ["500", 30, 1.4655593551538837, 0.40187541862022774], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.NoHttpResponseException/Non HTTP response message: www.carpool.com:80 failed to respond", 2, 0.09770395701025891, 0.02679169457468185], "isController": false}, {"data": ["Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.facebook.co.uk: Nombre o servicio desconocido", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["403", 1623, 79.28676111382511, 21.74146014735432], "isController": false}, {"data": ["Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.merriam-webster.co.uk: No existe ninguna direcci&oacute;n asociada al nombre", 4, 0.19540791402051783, 0.0535833891493637], "isController": false}, {"data": ["Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.cafedecoco.com: Nombre o servicio desconocido", 2, 0.09770395701025891, 0.02679169457468185], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 7465, 2047, "403", 1623, "403/Forbidden", 202, "400", 70, "500", 30, "404/Not Found", 14], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": ["HTTP Request-3", 200, 6, "403/Forbidden", 3, "404/Not Found", 3, "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["HTTP Request-2", 640, 57, "403/Forbidden", 41, "429/Too Many Requests", 5, "503/Service Unavailable", 3, "406/Not Acceptable", 2, "401/HTTP Forbidden", 2], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["HTTP Request-4", 44, 1, "403/Forbidden", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["HTTP Request-1", 772, 94, "403/Forbidden", 56, "400/Bad Request", 6, "404/Not Found", 4, "Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.booking.co.uk: Nombre o servicio desconocido", 2, "Non HTTP response code: java.net.UnknownHostException/Non HTTP response message: www.booking.de: Nombre o servicio desconocido", 2], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["HTTP Request-21", 4, 4, "Non HTTP response code: java.io.IOException/Non HTTP response message: Exceeded maximum number of redirects: 20", 4, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["HTTP Request", 4965, 1885, "403", 1623, "403/Forbidden", 101, "400", 70, "500", 30, "404/Not Found", 7], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
