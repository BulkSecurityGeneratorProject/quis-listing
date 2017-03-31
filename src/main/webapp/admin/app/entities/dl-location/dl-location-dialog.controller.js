(function () {
    'use strict';

    angular
        .module('quisListingApp')
        .controller('DlLocationDialogController', DlLocationDialogController)
        .filter('range', function () {
            return function (input, total) {
                total = parseInt(total);
                for (var i = 0; i < total; i++)
                    input.push(i);
                return input;
            };
        });

    DlLocationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'DlLocation', 'AlertService'];

    function DlLocationDialogController($timeout, $scope, $stateParams, $uibModalInstance, entity, DlLocation, AlertService) {
        var vm = this;
        vm.predicate = 'id';
        vm.reverse = true;

        loadAll();

        vm.onSelectCallback = function ($item, $model) {
            console.log("Item:");
            console.log($item);
            console.log("Model:");
            console.log($model);
        };

        function loadAll() {

            DlLocation.query({
                sort: sort(),
                languageCode: entity.languageCode
            }, onSuccess, onError);

            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }

            function onSuccess(data, headers) {
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.dlLocations = data;
            }

            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        vm.dlLocation = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function save() {
            vm.isSaving = true;
            if (vm.dlLocation.id !== null) {
                DlLocation.update(vm.dlLocation, onSaveSuccess, onSaveError);
            } else {
                DlLocation.save(vm.dlLocation, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess(result) {
            $scope.$emit('quisListingApp:dlLocationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }

    }
})();
