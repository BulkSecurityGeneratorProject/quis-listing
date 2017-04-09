(function () {
    'use strict';

    angular
        .module('quisListingApp')
        .controller('DlListingDialogController', DlListingDialogController)
        .controller('FileDestroyController', [
            '$scope', '$http',
            function ($scope, $http) {
                var file = $scope.file,
                    state;
                if (file.url) {
                    file.$state = function () {
                        return state;
                    };
                    file.$destroy = function () {
                        state = 'pending';
                        return $http({
                            url: file.deleteUrl,
                            method: file.deleteType
                        }).then(
                            function () {
                                state = 'resolved';
                                $scope.clear(file);
                            },
                            function () {
                                state = 'rejected';
                            }
                        );
                    };
                } else if (!file.$cancel && !file._index) {
                    file.$cancel = function () {
                        $scope.clear(file);
                    };
                }
            }
        ])
    ;

    DlListingDialogController.$inject = ['$http', '$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity',
        'DlListing', 'DlCategory', 'DlContentField'];

    function DlListingDialogController($http, $timeout, $scope, $stateParams, $uibModalInstance, entity,
                                       DlListing, DlCategory, DlContentField) {
        var vm = this;
        vm.predicate = 'id';
        vm.reverse = true;

        vm.dlListing = entity;
        vm.clear = clear;
        vm.fileUploadOptions = {
            url: '/api/admin/files'
        };
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;

        vm.loadUploadedFiles = loadUploadedFiles;
        vm.loadCategories = loadCategories;
        vm.loadDlContentFields = loadDlContentFields;
        vm.parentId = null;
        // vm.dlContentFields = [
        //     {
        //         name: "Phone",
        //         type: "string",
        //         value: ""
        //     },
        //     {
        //         name: "Hair",
        //         type: "select",
        //         value: ""
        //     }
        // ];

        $scope.tinymceOptions = {
            menubar: false,
            plugins: 'code',
            toolbar: 'undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image | code',
        };

        loadUploadedFiles();
        loadCategories();
        loadDlContentFields();

        function loadCategories() {
            DlCategory.query({
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
                vm.dlCategoryTotalItems = headers('X-Total-Count');
                vm.dlCategoryQueryCount = vm.dlCategoryTotalItems;

                vm.dlCategories = data;
            }

            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadUploadedFiles() {
            if (vm.dlListing.id !== null) {
                $scope.loadingFiles = true;
                $http.get(url)
                    .then(
                        function (response) {
                            $scope.loadingFiles = false;
                            $scope.queue = response.data.files || [];
                        },
                        function () {
                            $scope.loadingFiles = false;
                        }
                    );
            }
        }

        function loadDlContentFields() {
            DlContentField.query({
                sort: sort()
            }, onSuccess, onError);

            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }

            function onSuccess(data, headers) {
                vm.dlContentFieldTotalItems = headers('X-Total-Count');
                vm.dlContentFieldQueryCount = vm.dlCategoryTotalItems;

                for (var i = 0; i < data.length; i++) {
                    var dlContentField = data[i];
                    if (dlContentField.options) {
                        dlContentField.optionsModel = JSON.parse(dlContentField.options);
                    }
                }

                vm.dlContentFields = data;
            }

            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function save() {
            vm.isSaving = true;
            if (vm.dlListing.id !== null) {
                DlListing.update(vm.dlListing, onSaveSuccess, onSaveError);
            } else {
                DlListing.save(vm.dlListing, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess(result) {
            $scope.$emit('quisListingApp:dlListingUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.expirationDate = false;

        function openCalendar(date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
