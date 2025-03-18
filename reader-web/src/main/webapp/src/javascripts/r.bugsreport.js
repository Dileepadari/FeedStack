/**
 *  Bugs report module.
 */

r.bugsreport.reset = function() {
    $('#bugsreport-container').hide();
};

r.bugsreport.report = function() {
    var email = r.user.userInfo.email;
    var description = $('#bugsreport-message').val();

    if (description.length === 0) {
        $().toastmessage('showErrorToast', $.t('bugsreport.error.empty'));
        return;
    }

    if (email.length === 0) {
        $().toastmessage('showErrorToast', $.t('bugsreport.error.invalid_email'));
        return;
    }

    var data = {
        description: description,
        email: email
    };

    $.ajax({
        type: 'POST',
        url: r.util.url.report_bug,
        data: JSON.stringify(data),
        contentType: 'application/json',
        success: function(data) {
            $().toastmessage('showSuccessToast', $.t('bugsreport.success'));
        },
        error: function(data) {
            $().toastmessage('showErrorToast', $.t('bugsreport.error'));
        }
    });

    $('#bugsreport-message').val('');
};

r.bugsreport.init = function() {
    $.History.bind('/bugsreport/', function(state, target) {
        r.main.reset();
        $('#bugsreport-container').show();
        $('#toolbar > .settings').removeClass('hidden');
    });

    if (!r.user.hasBaseFunction('ADMIN')) {
        $('#bugsreport-tab-admin').remove();
        var userEmail = r.user.userInfo.email;
        r.bugsreport.getUserBugs(userEmail);
    }

    if (r.user.hasBaseFunction('ADMIN')) {
        $('#bugsreport-tab-users').remove();
        r.bugsreport.getAllBugs();
    }

    $('#bugsreport-form').on('submit', function (event) {
        event.preventDefault();
        r.bugsreport.report();
        return false;
    });

    $('#buglogs-refresh-button').on('click', function (event) {
        if (r.user.hasBaseFunction('ADMIN')) {
            r.bugsreport.getAllBugs();
        } else {
            var userEmail = r.user.userInfo.email;
            r.bugsreport.getUserBugs(userEmail);
        }
        return false;
    });
};

r.bugsreport.deleteBugReport = function(bugId) {
    $.ajax({
        type: 'DELETE',
        url: r.util.url.bugs_report_delete.replace('{id}', bugId),
        success: function(data) {
            if (r.user.hasBaseFunction('ADMIN')) {
                r.bugsreport.getAllBugs();
            } else {
                var userEmail = r.user.userInfo.email;
                r.bugsreport.getUserBugs(userEmail);
            }
            $().toastmessage('showSuccessToast', $.t('bugsreport.deleted'));
        },
        error: function(data) {
            $().toastmessage('showErrorToast', $.t('bugsreport.delete.error'));
        }
    });
};

r.bugsreport.updateBugReportStatus = function(bugId, newStatus) {
    $.ajax({
        type: 'POST',
        url: r.util.url.bugs_report_updatestatus,
        data: JSON.stringify({
            id: bugId,
            status: newStatus
        }),
        contentType: 'application/json',
        success: function(data) {
            r.bugsreport.getAllBugs();
            $().toastmessage('showSuccessToast', $.t('Status Updated Successfully'));
        },
        error: function(data) {
            $().toastmessage('showErrorToast', $.t('bugsreport.update.error'));
        }
    });
};

r.bugsreport.getAllBugs = function() {
    $.ajax({
        type: 'GET',
        url: r.util.url.bugs_report_getall,
        success: function(data) {
            var bugReports = data.items;
            var tableBody = $('#bugsreport-table tbody');
            tableBody.empty();

            $.each(bugReports, function(index, bugReport) {
                var row = '<tr class="bug-item" id="' + bugReport.id +'">' +
                    '<td>' +  (index  + 1) + '</td>' +
                    '<td>' + bugReport.email + '</td>' +
                    '<td class="bug-description">' + bugReport.description + '</td>' +
                    '<td>' + bugReport.timestamp + '</td>' +
                    '<td class="bug-status-parent">' +
                        '<select class="bug-status" data-id="' + bugReport.id + '">' +
                            '<option value="OPEN"' + (bugReport.status === 'OPEN' ? ' selected' : '') + '>Open</option>' +
                            '<option value="CLOSED"' + (bugReport.status === 'CLOSED' ? ' selected' : '') + '>Closed</option>' +
                            '<option value="IN_PROGRESS"' + (bugReport.status === 'IN_PROGRESS' ? ' selected' : '') + '>In Progress</option>' +
                            '<option value="RESOLVED"' + (bugReport.status === 'RESOLVED' ?'selected' : '') + '>Resolved</option>' +
                        '</select>' +
                    '</td>' +
                    '<td><button class="delete-bug" data-id="' + bugReport.id + '">Delete</button></td>' +
                    '</tr>';
                tableBody.append(row);
            });

            $('.bug-status').on('change', function() {
                var bugId = $(this).data('id');
                var newStatus = $(this).val();
                r.bugsreport.updateBugReportStatus(bugId, newStatus);
            });

            $('.delete-bug').on('click', function() {
                var bugId = $(this).data('id');
                if (confirm('Are you sure you want to delete this bug?')){
                    r.bugsreport.deleteBugReport(bugId);
                }
            });
        },
        error: function(data) {
            console.error("Error: " + data);
        }
    });
};

r.bugsreport.getUserBugs = function(email) {
    $.ajax({
        type: 'GET',
        url: r.util.url.bugs_report_getbyemail,
        data: { email: email },
        success: function(data) {
            var bugReports = data.items;
            var tableBody = $('#bugsreport-user-table tbody');
            tableBody.empty();

            $.each(bugReports, function(index, bugReport) {
                var row = '<tr class="bug-item" id="' + bugReport.id +'">' +
                    '<td>' +  (index  + 1) + '</td>' +
                    '<td class="bug-description">' + bugReport.description + '</td>' +
                    '<td>' + bugReport.timestamp + '</td>' +
                    '<td>' + bugReport.status + '</td>' +
                    '<td><button class="delete-bug" data-id="' + bugReport.id + '">Delete</button></td>' +
                    '</tr>';
                tableBody.append(row);
            });

            $('.delete-bug').on('click', function() {
                var bugId = $(this).data('id');
                if (confirm('Are you sure you want to delete this bug?')){
                    r.bugsreport.deleteBugReport(bugId);
                }
            });
        },
        error: function(data) {
            console.error("Error: " + data);
        }
    });
};

r.bugsreport.filterBugs = function(criteria) {
    $.ajax({
        type: 'GET',
        url: r.util.url.bugs_report_getall,
        data: criteria,
        success: function(data) {
            var bugReports = data.bugReports;
            var tableBody = $('#bugsreport-table tbody');
            tableBody.empty();

            bugReports.forEach(function(bugReport) {
                var row = '<tr>' +
                    '<td>' + bugReport.id + '</td>' +
                    '<td>' + bugReport.email + '</td>' +
                    '<td>' + bugReport.description + '</td>' +
                    '<td>' + bugReport.timestamp + '</td>' +
                    '<td>' + bugReport.status + '</td>' +
                    '</tr>';
                tableBody.append(row);
            });
        },
        error: function(data) {
            console.error("Error: " + data);
        }
    });
};

$('#filter-form').on('submit', function(event) {
    event.preventDefault();
    var criteria = {
        status: $('#filter-status').val(),
        email: $('#filter-email').val()
    };
    r.bugsreport.filterBugs(criteria);
});