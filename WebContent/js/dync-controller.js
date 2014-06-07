(function ($) {
    /*
     BackBone.js Based Application - Dync
     */
    var parseURL = 'http://localhost:8080/Dync/usercontrol';
    Kakao.init('07f2d3ff4958ad3553bc8830de72133b');
// Models
    Issue = Backbone.Model.extend();
    Tag = Backbone.Model.extend();
    Login = Backbone.Model.extend({
        url: parseURL,
        defaults: {
            logged: 'no',
            naver_hash: null,
            kakao_hash: null
        }
    });
// Collections
    IssueCollection = Backbone.Collection.extend({
        model: Issue,
        url: 'http://localhost:8080/Dync/issuecontrol?action=list',
        parse: function (response) {
            return response.results;
        },
        sync: function (method, model, options) {
            var that = this;
            var params = _.extend({
                type: 'GET',
                dataType: 'json',
                url: that.url,
                processData: false
            }, options);
            return $.ajax(params);
        }
    });


// View
    IssuelistView = Backbone.View.extend({
        el: $('div#Issuelist'),

        initialize: function () {
            var that = this;
            _.bindAll(this, 'render');
            this.collection = new IssueCollection();
            this.collection.fetch({
                success: function (data, res) {
                    that.render(res);
                }
            });
        },
        template: _.template($('#listIssueTemplate').html()),
        render: function (res) {
            $(this.el).html(this.template({ issues: res }));

        }
    });
    LoginView = Backbone.View.extend({
        el: $(".user"),
        events: {
            "click #login-btn": "loginpopup"
        },
        initialize: function () {
            //this.render();
            var that = this;
            $.ajax({
                type: "GET",
                url: parseURL,
                dataType: 'json',
                data: { action: 'check' },
                success: function (args) {
                    console.log(args.logged);
                    that.model.set({'logged': args.logged, 'kakao_hash': args.kakao_hash});
                    that.render();
                    //that.naver_hash = auth.naver_hash;
                }
            });
            _.bindAll(this, "render");
            this.model.bind('change', this.render);
        },
        render: function () {
            var status = this.model.get("logged");
            console.log(status);
            if (status == "ok") {
                $(".lightbox_container").hide(); // 로그인 성공시
                $("#login-btn").html("로그아웃");
            } else {
                console.log("Do login");
                $("#login-btn").html("로그인");
            }
        },
        loginpopup: function () {
            var status = this.model.get("logged");
            if (status == "ok") {
                alert("로그아웃");
                Kakao.Auth.logout();
                $.ajax({
                    type: "POST",
                    url: parseURL,
                    data: { action: 'logout'},
                    success: function (args) {
                        var auth = args.toJSON;
                        KakaoLogin.set({logged: args.logged, kakao_hash: null, naver_hash: null});
                    },
                    error: function (e) {
                        alert(e.responseText);
                    }
                });
            } else {
                $(".lightbox_container").show();
                $("#lightbox .close").bind("click", function () {
                    $("#underlayer").hide();
                });
            }
        }
    });

// External Operation
    var KakaoLogin = new Login();
    var LoginSync = new LoginView({model: KakaoLogin});
    var APIlogin = function () {
        Kakao.Auth.createLoginButton({
            container: '#kakao-login-btn',
            success: function (authObj) {
                console.log(authObj);
                Kakao.API.request({
                    url: '/v1/user/me',
                    success: function (res) {
                        var kakao = { action: 'login', logged: 'ok', kakao_hash: res.id, user_name: res.properties.nickname };
                        $.ajax({
                            type: "POST",
                            url: parseURL,
                            data: kakao,
                            callback: '?',
                            success: function (args) {
                                KakaoLogin.set({logged: 'ok'});
                            },
                            error: function (e) {
                                alert(e.responseText);
                            }
                        });
                    }
                });
            }
        });
    }
    new APIlogin;
// Operation
    new IssuelistView();

    var Coder = null;
    $("#left-menu-code, #add-code").click(function () {
        $("#repository").toggle("slow");
        if (!Coder) {
            Coder = CodeMirror.fromTextArea(document.getElementById("texteditor"), {
                lineNumbers: true,
                mode: "javascript",
                keyMap: "sublime",
                styleActiveLine: true,
                autoCloseBrackets: true,
                matchBrackets: true,
                showCursorWhenSelecting: true,
                theme: "mbo"
            });
            Coder.doc.setValue("//코드를 미리 입력하세요!");
        }
        Coder.on("change", function(cm, n) {
            $("#editor_content").val(cm.getValue());
        });
    });
    $("#left-menu-timeline").click(function () {
        $("#left-menu-tag").toggle("slow");
    });
    $(".winclose").click(function () {
        $("#repository").hide();
    });
    $("#send-btn").click(function () {
        if (KakaoLogin.get("logged") == "ok") {
            var Send = confirm("새로운 이슈를 등록하시겠습니까?");
            if (Send == true) {
                var options = {
                    target: '#insertIssueForm',
                    url: 'http://localhost:8080/Dync/issuecontrol',
                    resetForm: true,
                    success: function (args) {
                        alert("이슈가 등록되었습니다!");
                        location.reload();
                    }
                };
                $("#insertIssueForm").ajaxSubmit(options);
            } else {
                return false;
            }
        } else {
            alert("로그인 후 등록할 수 있습니다.");
            return false;
        }
    });

    $("#save-btn").click(function (e) {
        if (KakaoLogin.get("logged") == "ok") {
            if ($("#input-subject").val() != ""){
                $("#insertCodeForm").ajaxSubmit();
            } else {
                return false;
            }
        } else {
            alert("로그인 후 등록할 수 있습니다.");
            return false;
        }
        return false;
    });
    function nl2br(value) {
        return value.replace(/\n/g, "<br />");
    }
//IssueView();

})(jQuery);