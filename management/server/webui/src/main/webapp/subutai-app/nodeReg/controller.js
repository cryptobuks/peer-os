'use strict';
angular.module('subutai.nodeReg.controller', [])
    .controller('NodeRegCtrl', NodeRegCtrl);

NodeRegCtrl.$inject = [ 'nodeRegSrv', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder' ];

function NodeRegCtrl(nodeRegSrv, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder) {
    var vm = this;
	vm.action = 'install';
	vm.nodes = [];

	//functions
	vm.approveNode = approveNode;

	vm.dtOptions = DTOptionsBuilder
			.newOptions()
			.withOption('order', [[1, "asc" ]])
			.withOption('stateSave', true)
			.withPaginationType('full_numbers');

	vm.dtColumnDefs = [
		DTColumnDefBuilder.newColumnDef(0).notSortable(),
		DTColumnDefBuilder.newColumnDef(1),
		DTColumnDefBuilder.newColumnDef(2)
	];


	nodeRegSrv.getData().success(function(data){
		console.log(data,"<<<<<<<<<<<<<<<<<look here");
		vm.nodes = data;
	});


	function approveNode(nodeId) {

		if(nodeId === undefined) return;

		SweetAlert.swal("Success!", "Node is being approved.", "success");

		nodeRegSrv.approveNode( nodeId ).success(function (data) {
			SweetAlert.swal(
				"Success!",
				"Node has been added to cluster.",
				"success"
			);

			// @todo add refresh table
		});
	}
};
