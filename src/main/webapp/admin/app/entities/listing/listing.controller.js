(function() {
    'use strict';

    angular
        .module('quisListingApp')
        .controller('ListingController', ListingController);

    ListingController.$inject = ['$scope', '$state', 'Listing', 'ListingSearch', 'ParseLinks', 'AlertService',
        'paginationConstants', 'pagingParams', 'DataLanguageHub'];

    function ListingController ($scope, $state, Listing, ListingSearch, ParseLinks, AlertService,
                                paginationConstants, pagingParams, DataLanguageHub) {
        var vm = this;

        vm.dataLanguageHub = DataLanguageHub.get();
        vm.loadPage = loadPage;
        vm.predicate = pagingParams.predicate;
        vm.reverse = pagingParams.ascending;
        vm.transition = transition;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.searchQuery = pagingParams.search;
        vm.currentSearch = pagingParams.search;
        vm.clear = clear;
        vm.search = search;
        vm.loadAll = loadAll;
        vm.onLanguageChange = onLanguageChange;

        vm.selectedLanguageCode = vm.dataLanguageHub.selectedLanguageCode;
        vm.activeLanguages = [
            {code: "en", englishName: "English", count: 0}
        ];

        loadActiveLanguages();
        loadAll();

        function loadActiveLanguages() {
            Listing.activeLanguages({

            }, onSuccess, onError);
            function onSuccess(data, headers) {
                vm.activeLanguages = data;
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function loadAll () {
            if (pagingParams.search) {
                ListingSearch.query({
                    query: pagingParams.search,
                    page: pagingParams.page - 1,
                    size: vm.itemsPerPage,
                    sort: sort()
                }, onSuccess, onError);
            } else {
                Listing.query({
                    page: pagingParams.page - 1,
                    size: vm.itemsPerPage,
                    sort: sort(),
                    languageCode: vm.selectedLanguageCode
                }, onSuccess, onError);
            }
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }
            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = vm.totalItems;
                vm.listings = data;
                vm.page = pagingParams.page;
            }
            function onError(error) {
                AlertService.error(error.data.message);
            }
        }

        function onLanguageChange() {
            loadAll();
            vm.dataLanguageHub.selectedLanguageCode = vm.selectedLanguageCode;
        }

        function loadPage(page) {
            vm.page = page;
            vm.transition();
        }

        function transition() {
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch
            });
        }

        function search(searchQuery) {
            if (!searchQuery){
                return vm.clear();
            }
            vm.links = null;
            vm.page = 1;
            vm.predicate = '_score';
            vm.reverse = false;
            vm.currentSearch = searchQuery;
            vm.transition();
        }

        function clear() {
            vm.links = null;
            vm.page = 1;
            vm.predicate = 'id';
            vm.reverse = true;
            vm.currentSearch = null;
            vm.transition();
        }
    }
})();
