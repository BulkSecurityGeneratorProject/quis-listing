EditListing = {
    init: function (dlListingDTO, dlCategoriesDtoFlat, dlContentFieldsDto, dlLocationCountries, dlLocationStates, dlLocationCities, commonVar) {
        Vue.use(window.vuelidate.default);
        const {required, minLength, maxLength, between, email, sameAs} = window.validators;

        /**
         * @return {boolean}
         */
        const TwoWordValidator = function (value, component) {
            var s = value;
            s = s.replace(/(^\s*)|(\s*$)/gi, "");
            s = s.replace(/[ ]{2,}/gi, " ");
            s = s.replace(/\n /, "\n");
            return s.split(' ').length >= 2;
        };

        const touchMap = new WeakMap();

        $.danidemo = $.extend({}, {

            addLog: function (id, status, str) {
                var d = new Date();
                var li = $('<li />', {'class': 'demo-' + status});

                var message = '[' + d.getHours() + ':' + d.getMinutes() + ':' + d.getSeconds() + '] ';

                message += str;

                li.html(message);

                $(id).prepend(li);
            },

            addFile: function (id, i, file) {
                var template = '<div id="demo-file' + i + '">' +
                    '<span class="demo-file-id">#' + i + '</span> - ' + file.name + ' <span class="demo-file-size">(' + $.danidemo.humanizeSize(file.size) + ')</span> - Status: <span class="demo-file-status">Waiting to upload</span>' +
                    '<div class="progress progress-striped active">' +
                    '<div class="progress-bar" role="progressbar" style="width: 0%;">' +
                    '<span class="sr-only">0% Complete</span>' +
                    '</div>' +
                    '</div>' +
                    '</div>';

                var i = $(id).attr('file-counter');
                if (!i) {
                    $(id).empty();

                    i = 0;
                }

                i++;

                $(id).attr('file-counter', i);

                $(id).prepend(template);
            },

            updateFileStatus: function (i, status, message) {
                $('#demo-file' + i).find('span.demo-file-status').html(message).addClass('demo-file-status-' + status);
            },

            updateFileProgress: function (i, percent) {
                $('#demo-file' + i).find('div.progress-bar').width(percent);

                $('#demo-file' + i).find('span.sr-only').html(percent + ' Complete');
            },

            humanizeSize: function (size) {
                var i = Math.floor(Math.log(size) / Math.log(1024));
                return ( size / Math.pow(1024, i) ).toFixed(2) * 1 + ' ' + ['B', 'kB', 'MB', 'GB', 'TB'][i];
            }

        }, $.danidemo);

        $('#drag-and-drop-zone').dmUploader({
            url: '/api/dl-listing/' + dlListingDTO.id + '/upload',
            dataType: 'json',
            allowedTypes: 'image/*',
            onInit: function () {
                $.danidemo.addLog('#demo-debug', 'default', 'Plugin initialized correctly');
            },
            onBeforeUpload: function (id) {
                $.danidemo.addLog('#demo-debug', 'default', 'Starting the upload of #' + id);

                $.danidemo.updateFileStatus(id, 'default', 'Uploading...');
            },
            onNewFile: function (id, file) {
                $.danidemo.addFile('#demo-files', id, file);
            },
            onComplete: function () {
                $.danidemo.addLog('#demo-debug', 'default', 'All pending tranfers completed');
            },
            onUploadProgress: function (id, percent) {
                var percentStr = percent + '%';

                $.danidemo.updateFileProgress(id, percentStr);
            },
            onUploadSuccess: function (id, data) {
                $.danidemo.addLog('#demo-debug', 'success', 'Upload of file #' + id + ' completed');

                $.danidemo.addLog('#demo-debug', 'info', 'Server Response for file #' + id + ': ' + JSON.stringify(data));

                $.danidemo.updateFileStatus(id, 'success', 'Upload Complete');

                $.danidemo.updateFileProgress(id, '100%');
            },
            onUploadError: function (id, message) {
                $.danidemo.updateFileStatus(id, 'error', message);

                $.danidemo.addLog('#demo-debug', 'error', 'Failed to Upload file #' + id + ': ' + message);
            },
            onFileTypeError: function (file) {
                $.danidemo.addLog('#demo-debug', 'error', 'File \'' + file.name + '\' cannot be added: must be an image');
            },
            onFileSizeError: function (file) {
                $.danidemo.addLog('#demo-debug', 'error', 'File \'' + file.name + '\' cannot be added: size excess limit');
            },
            /*onFileExtError: function(file){
             $.danidemo.addLog('#demo-debug', 'error', 'File \'' + file.name + '\' has a Not Allowed Extension');
             },*/
            onFallbackMode: function (message) {
                $.danidemo.addLog('#demo-debug', 'info', 'Browser not supported(do something else here!): ' + message);
            }
        });


        var roots = MyListingsComponent.Utils.flatItemsToTree(dlCategoriesDtoFlat);

        for (var i = 0; i < dlContentFieldsDto.length; i++) {
            var dlContentField = dlContentFieldsDto[i];

            if (dlContentField.options) {
                dlContentField.optionsModel = JSON.parse(dlContentField.options);
            }

            dlContentField.value = getValueFromContentField(dlContentField, dlListingDTO.dlListingFields);
        }

        function getValueFromContentField(dlContentField, dlListingFields) {
            if (dlListingFields) {
                for (let dlListingField of dlListingFields) {
                    if (dlListingField.id == dlContentField.id) {
                        if (dlContentField.type == 'CHECKBOX') {
                            if (dlListingField.value) {
                                return JSON.parse(dlListingField.value);
                            } else {
                                return [];
                            }
                        } else {
                            return dlListingField.value;
                        }
                    }
                }
            } else {
                if (dlContentField.type == 'CHECKBOX') {
                    // return empty array to be able to operate checkbox
                    return [];
                }
            }
            return null;
        }

        var selectedCategory = {
            name: ''
        };
        if (dlListingDTO.dlCategories && dlListingDTO.dlCategories.length > 0) {
            selectedCategory = dlListingDTO.dlCategories[0];
        }

        var selectedCityId = -1;
        var selectedStateId = -1;
        var selectedCountryId = -1;
        if (dlListingDTO.dlLocations && dlListingDTO.dlLocations.length > 0) {
            selectedCityId = dlListingDTO.dlLocations[0].id;
            selectedStateId = dlListingDTO.dlLocations[0].parentId;
            selectedCountryId = dlListingDTO.dlLocations[0].parent.parent.id;
        }

        var editListingApp = new Vue({
            el: '#editListingApp',
            data: {
                queuePos: 0,
                queueFiles: [],
                categories: roots,
                dlContentFields: dlContentFieldsDto,
                selectedCountry: selectedCountryId,
                selectedState: selectedStateId,
                selectedCity: selectedCityId,
                selectedCategory: selectedCategory,
                dlLocationCountries: dlLocationCountries,
                dlLocationStates: dlLocationStates,
                dlLocationCities: dlLocationCities,
                listing: {
                    id: dlListingDTO.id,
                    title: dlListingDTO.title,
                    content: dlListingDTO.content,
                    name: dlListingDTO.name,
                    status: dlListingDTO.status,
                    dlCategories: dlListingDTO.dlCategories,
                    dlLocations: dlListingDTO.dlLocations,
                    dlListingFields: dlListingDTO.dlListingFields,
                    attachments: dlListingDTO.attachments
                },
                image: ''
            },
            validations: {
                listing: {}
            },
            methods: {
                fileUpload: function (files) {
                    console.log(files);

                    var index = this.queueFiles.push({
                        file: files[0],
                        progress: 0
                    });
                    var local = this.queueFiles;
                    var listing = this.listing;
                    var fd = new FormData();
                    fd.append(files[0].fileName, files[0]);
                    // Ajax Submit
                    $.ajax({
                        url: '/api/dl-listings/' + this.listing.id + '/upload',
                        type: 'POST',
                        dataType: 'json',
                        data: fd,
                        cache: false,
                        contentType: false,
                        processData: false,
                        forceSync: false,
                        xhr: function () {
                            var xhrobj = $.ajaxSettings.xhr();
                            if (xhrobj.upload) {
                                xhrobj.upload.addEventListener('progress', function (event) {
                                    var percent = 0;
                                    var position = event.loaded || event.position;
                                    var total = event.total || event.totalSize;
                                    if (event.lengthComputable) {
                                        percent = Math.ceil(position / total * 100);
                                    }

                                    // widget.settings.onUploadProgress.call(widget.element, widget.queuePos, percent);
                                    console.log(percent);
                                    local[index-1].progress = percent;
                                }, false);
                            }

                            return xhrobj;
                        },
                        success: function (data, message, xhr) {
                            // widget.settings.onUploadSuccess.call(widget.element, widget.queuePos, data);
                            console.log("Success upload");
                            listing.attachments = data.attachments;
                            $.notify({
                                message: 'Success upload'
                            }, {
                                type: 'success'
                            });

                        },
                        error: function (xhr, status, errMsg) {
                            // widget.settings.onUploadError.call(widget.element, widget.queuePos, errMsg);
                            console.log("Error upload");
                        },
                        complete: function (xhr, textStatus) {
                            // widget.processQueue();
                            console.log("Complete upload");
                        }
                    });

                },
                onDrop: function (e) {
                    e.stopPropagation();
                    e.preventDefault();
                    var files = e.dataTransfer.files;
                    this.fileUpload(files);
                },
                onChange: function (e) {
                    var files = e.target.files;
                    this.fileUpload(files);
                },
                createFile: function (file) {
                    if (!file.type.match('image.*')) {
                        alert('Select an image');
                        return;
                    }
                    var img = new Image();
                    var reader = new FileReader();
                    var vm = this;

                    reader.onload = function (e) {
                        vm.image = e.target.result;
                    };
                    reader.readAsDataURL(file);
                },
                removeImage: function (attachment) {
                    this.$http({url: '/api/dl-listings/'+this.listing.id+'/attachments/'+attachment.id, method: 'DELETE'}).then(function (response) {
                        console.log('Success!:', response.data);

                        let index = this.listing.attachments.indexOf(attachment);
                        this.listing.attachments.splice(index, 1);
                        $.notify({
                            message: response.headers.get('X-qlService-alert')
                        }, {
                            type: 'success'
                        });
                    }, function (response) {
                        console.log('Error!:', response.data);
                        $.notify({
                            message: response.data
                        }, {
                            type: 'danger'
                        });
                    });
                },
                onBoxClick: function (e) {
                    $(e.target.firstElementChild).trigger('click');
                },
                onCountryChange: function () {
                    if (this.selectedCountry === "-1") {
                        this.dlLocationStates = [];
                        this.selectedState = -1;
                        this.dlLocationCities = [];
                        this.selectedCity = -1;
                    } else {
                        var params = {
                            parentId: this.selectedCountry
                        };

                        this.$http({url: '/api/dl-locations', params: params, method: 'GET'}).then(function (response) {
                            console.log('Success!:', response.data);
                            this.dlLocationStates = response.data;
                        }, function (response) {
                            console.log('Error!:', response.data);
                            $.notify({
                                message: response.data
                            }, {
                                type: 'danger'
                            });
                        });
                    }

                },
                onStateChange: function () {
                    if (this.selectedState === "-1") {
                        this.dlLocationCities = [];
                        this.selectedCity = -1;
                    } else {
                        var params = {
                            parentId: this.selectedState
                        };
                        this.$http({url: '/api/dl-locations', params: params, method: 'GET'}).then(function (response) {
                            console.log('Success!:', response.data);
                            this.dlLocationCities = response.data;
                        }, function (response) {
                            console.log('Error!:', response.data);
                            $.notify({
                                message: response.data
                            }, {
                                type: 'danger'
                            });
                        });
                    }
                },
                delayTouch: function ($v) {
                    $v.$reset();
                    if (touchMap.has($v)) {
                        clearTimeout(touchMap.get($v));
                    }
                    touchMap.set($v, setTimeout($v.$touch, 1000))
                },
                onSave: function (event) {
                    var $btn = $('#btnSave').button('loading');

                    let dlListingFields = [];
                    for (let dlContentField of this.dlContentFields) {
                        let value;
                        if (dlContentField.type == 'CHECKBOX') {
                            if (dlContentField.value) {
                                value = JSON.stringify(dlContentField.value);
                            } else {
                                value = JSON.stringify([]);
                            }
                        } else {
                            value = dlContentField.value;
                        }
                        let listingField = {
                            id: dlContentField.id,
                            value: value
                        };
                        dlListingFields.push(listingField)
                    }

                    this.listing.dlListingFields = dlListingFields;
                    if (this.selectedCity !== "-1") {
                        this.listing.dlLocations = [{
                            id: this.selectedCity
                        }];
                    } else {
                        this.listing.dlLocations = [];
                    }

                    this.listing.dlCategories = [this.selectedCategory];

                    var payload = this.listing;

                    this.$http({url: '/api/dl-listings', body: payload, method: 'PUT'}).then(function (response) {
                        console.log('Success!:', response.data);

                        $.notify({
                            message: response.headers.get('X-qlService-alert')
                        }, {
                            type: 'success'
                        });
                        $btn.button('reset');
                    }, function (response) {
                        console.log('Error!:', response.data);
                        $.notify({
                            message: response.data
                        }, {
                            type: 'danger'
                        });
                        $btn.button('reset');
                    });
                },
                onPublish: function (event) {
                },
                openCategorySelection: function ($v) {
                    // $v.$touch();
                    this.delayTouch($v);
                    $('#myModal').modal('toggle');
                }
            }, computed: {
                thumbnails: function (attachments) {
                    return attachments.filter(function (attachment) {
                        for (let imageResizeMeta of attachment.attachmentMetadata.imageResizeMetas) {
                            if (imageResizeMeta.name === 'dl-thumbnail') {
                                return true
                            }
                        }

                    });
                }
            }
        });

        commonVar.addListingApp = editListingApp;

        editListingApp.$on('id-selected', function (category) {
            if (this.selectedCategory) {
                this.selectedCategory.active = false;
            }
            this.selectedCategory = category;
            this.selectedCategory.active = true;
        });
    }
};