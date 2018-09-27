Ext.require([
    'Ext.selection.CellModel',
    'Ext.tab.*',
    'Ext.tree.*',
    'Ext.data.*',
    'Ext.tip.*'
]);
var json=null;


// get fields
function buildsFiels(json){
    var uniques=[];
    for (i = 0; i < json.length; i++) {
        for(x in json[i]) {
            if (!uniques.includes(x)) {
                uniques.push(x)
            }
        }
    }
    return uniques;
}
// create column info
function buildColumnsInfo(json) {
    var uniques=buildsFiels(json);
    var colsInfo=[];

      if (uniques.length>0){
          for(i=0; i<uniques.length; i++){
              colsInfo.push({
                  text: uniques[i],
                  dataIndex: uniques[i],
                  flex: 0,
                  editor: {
                      allowBlank: false
                  }
              });
          }
      }
    return colsInfo;
}

// unique
function onlyUnique(value, index, self) {
    return self.indexOf(value) === index;
}

function getUser(){
    return pnLogin.items.get(1).items.get(0).items.get(0).getValue();
}
function getPassword() {
    return pnLogin.items.get(1).items.get(0).items.get(1).getValue();
}
function getJdbc() {
    return pnLogin.items.get(0).getValue();
}
function getSQLStatement() {
    return pnSQL.items.get(0).getValue();
}
function getTime() {
    return pnLogin.items.get(1).items.get(1).items.get(0).getValue();
}
function getTimeOut() {
    return pnLogin.items.get(1).items.get(1).items.get(1).getValue();
}

// create model
function createModel( listOfField){
    var x = Ext.define('sqlModel', {
        extend: 'Ext.data.Model',
        fields: listOfField
    });
    return x;
}
// create store
function createStore(model, action) {
    var x = Ext.create('Ext.data.Store', {
        model: model,
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {
                act: action, //'DO_SELECT',
                user: getUser(),
                password: getPassword(),
                sql: getSQLStatement(),
                jdbc: getJdbc()
            },
            actionMethods: {
                read: 'POST'
            },
            reader: {
                type: 'json',
                root: 'data',
                totalProperty: 'total'
            },
            listeners: {
                exception: function (proxy, response, operation) {
                    handleError(operation);
                }
            }
        },
        autoLoad: true
    });
    return x;
}

// create grid
function createGrid(model, lst_columns, srows_, stime_){
    var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
        clicksToEdit: 1
    });

    var x = Ext.create('Ext.grid.Panel', {
        id: "sqlGrid",
        header: false,
        title: 'GRID',
        store: model,
        autoSizeColumn: true,
        columns: lst_columns,
        resizable: true,
        autoHeight: true,
        columnLines: true,
        overflowY: 'auto',
        overflowX: 'auto',
        tbar: [
            {
                xtype: 'button',
                cls: 'button-excel',
                scale: 'large',
                height: 20,
                margin: '0 1 0 1'
            }, {
                xtype: 'button',
                cls: 'button-save',
                scale: 'large',
                height: 20,
                margin: '0 1 0 1'
            }, {
                xtype: 'button',
                cls: 'button-open',
                scale: 'large',
                height: 20,
                margin: '0 1 0 1'
            }],

        bbar: {
            border: false,
            layout: 'anchor',
            items: [{
                xtype: 'textfield',
                name: 'rows',
                fieldLabel: 'Row count:',
                value: srows_,
                readOnly: true,
                padding: 1,
                fieldStyle: 'background-color: transparent; background-image: none;border : none;'
            },
                //Execution time
                {
                    xtype: 'textfield',
                    name: 'rows',
                    fieldLabel: 'Execution time:',
                    value: stime_,
                    reatdOnly: true,
                    padding: 1,
                    fieldStyle: 'background-color: transparent; background-image: none; border : none'
                }]
            // ,plugins: [cellEditing]
        }
    });
    return x;
}
// error Panel
function errorPanel() {
    var x = Ext.create('Ext.form.FormPanel', {
        title      : 'Sample TextArea',
        width      : '100%',
        header: false,
        bodyPadding: 10,
        height: 50,
        layout:{
            type:'fit'
        },
        items: [{
            ItemId: 'ExsqlTextId',
            id  :'idExSql',
            xtype     : 'textareafield',
            name      : 'message',
            fieldLabel: '',
            anchor    : '100%',
            width: '100%',
            fieldStyle: 'background-color: transparent; background-image: none; border : none',
            readOnly:true
        }]
    });
    return x;
}
function updateSQLGrid(){

}


function selectSQLGrid(action){
    Ext.Ajax.request({
        url: dataUrl,
        method:'POST',
        params: {
            act: action,
            user: getUser(),
            password: getPassword(),
            sql: getSQLStatement(),
            jdbc: getJdbc()
        },
        success: function(result){
           json = JSON.parse(result.responseText);
            pnResultSQL.setHeight(300);

            var resultsContainer = Ext.getCmp('resultSQL');
                resultsContainer.removeAll();

           if (json.data) {
               var listSqlFields = buildsFiels(json.data);
               var sqlModel = createModel(listSqlFields);

               var grid = createGrid(
                   createStore(sqlModel,action),
                   buildColumnsInfo(json.data),
                   json.detail.rows,
                   json.detail.time
               );
               resultsContainer.add(grid);
           } /*end of if*/
            else {
                var wError =errorPanel();
                wError.items.get(0).setValue("In position "+json.error.error_pos+", "+json.error.error_msg);
               pnResultSQL.setHeight(70);
               resultsContainer.add(wError);
                }
        }, /*end of success*/
        failure: function(f,a){
              Ext.MessageBox.alert("FUCK YOU!", "Ajax.request пиздец! Возможно Glassfish упал.");
         }

    }); /*end Ajax request*/
} /*end of function*/

    var pnSQL = new Ext.FormPanel({
        labelWidth:100,
        frame:true,
        title:'Query',
        //defaultType:'textfield',
        //resizable: true,
        monitorValid:true,
        items:[{
            xtype     : 'textareafield',
            itemId    : 'textAreaSQL',
            grow      : true,
            resizable: true,
            name      : 'sqlStatement',
            fieldLabel: '',
            anchor    : '100%',
            height: 120
             },
            {
            xtype:'button',
            cls: 'button-beauty',
            scale:'large',
            height:20,
            margin: '0 1 0 1'
            },
            {
                xtype:'panel',
                style: 'background-color: transparent; background-image: none; border : none',
                buttons: [
                    {
                        text: 'Select',
                        scale: 'small',
                        handler: function(){
                            selectSQLGrid('DO_SELECT');
                        }
                    },
                    {
                        text: 'Update',
                        scale: 'small',
                        handler: function(){

                        }
                    },{
                        text: 'Plan',
                        scale: 'small',
                        handler: function(){
                            selectSQLGrid('DO_PLAN');
                        }
                    },{
                        text:'Settings',
                        scale: 'small',
                        handler: function(){
                            pnLogin.toggleCollapse(true);
                        }
                    }]
            }]

    });
    var pnLogin = new Ext.Panel({
        title       : 'DB Connection strings',
        header: false,
        collapsible : true,
        collapsed: true,
        border: false,
        margin: '0 10 0 20',
        layout: 'vbox',
        items : [{
            xtype: 'textfield',
            name: 'dburl',
            fieldLabel: 'DB:',
            value: 'jdbc:oracle:thin:@172.17.110.145:1521:cdb',
            width:566,
            padding: '10 0 0 0',
            allowBlank: false  // requires a non-empty value
        },

            {
                layout: 'hbox',
                border: false,
                items:[
                    {
                        layout: 'vbox',
                        border: false,
                        items:[
                            {
                                xtype: 'textfield',
                                name: 'user_name',
                                fieldLabel: 'user name:',
                                value:'core',
                                padding: 0,
                                allowBlank: false  // requires a non-empty value
                            }, {
                                xtype: 'textfield',
                                name: 'password',
                                value:'core',
                                fieldLabel: 'password:',
                                padding: 1,
                                inputType: 'password',
                                vtype: 'password'  // requires value to be a valid email address format
                            }]
                    },
                    {
                        layout: 'vbox',
                        margin: '0 0 0 50',
                        border: false,
                        items:[
                            {
                                xtype: 'textfield',
                                name:'time',
                                value:'1000',
                                fieldLabel:'time:',
                                padding: 0
                            },
                            {
                                xtype: 'textfield',
                                name: 'time_out',
                                fieldLabel: 'time out:',
                                padding: 1,
                                value:'3000'
                            }]
                    }
                ]
            }

        ]

    });

    var pnResultSQL = Ext.create('Ext.Panel',{
        id:'resultSQL',
        title:'Results',
        collapsible : true,
        collapsed: false,
        layout: 'fit',
        height:300
    });

class Main {
 getPanelSQL(){
 }
 getPanelLogin(){
 }
 getResultPanel(){
 }
}
    Ext.onReady(function() {
        mainPanel = Ext.create('Ext.form.Panel',{
            width: '100%',
            maxheight: 600,
            autoHeight:true,
            renderTo    : 'sql-query-portlet-content',
            items : [ pnSQL,pnLogin,pnResultSQL]
        });
     });
new Main();