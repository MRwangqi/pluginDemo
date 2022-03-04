package com.codelang.plugin.html

object Html {
    fun getHtml(): String {
        return """
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
                    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml">

            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <title>Lint Report</title>
                <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
                <link rel="stylesheet" href="https://code.getmdl.io/1.2.1/material.blue-indigo.min.css"/>
                <link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Roboto:300,400,500,700"
                      type="text/css">
                <script defer src="https://code.getmdl.io/1.2.0/material.min.js"></script>
                <style>
            section.section--center {
                max-width: 860px;
            }
            .mdl-card__supporting-text + .mdl-card__actions {
                border-top: 1px solid rgba(0, 0, 0, 0.12);
            }
            main > .mdl-layout__tab-panel {
              padding: 8px;
              padding-top: 48px;
            }

            .mdl-card__actions {
                margin: 0;
                padding: 4px 40px;
                color: inherit;
            }
            .mdl-card > * {
                height: auto;
            }
            .mdl-card__actions a {
                color: #00BCD4;
                margin: 0;
            }
            .error-icon {
                color: #bb7777;
                vertical-align: bottom;
            }
            .warning-icon {
                vertical-align: bottom;
            }
            .mdl-layout__content section:not(:last-of-type) {
              position: relative;
              margin-bottom: 48px;
            }

            .mdl-card .mdl-card__supporting-text {
              margin: 40px;
              -webkit-flex-grow: 1;
                  -ms-flex-positive: 1;
                      flex-grow: 1;
              padding: 0;
              color: inherit;
              width: calc(100% - 80px);
            }
            div.mdl-layout__drawer-button .material-icons {
                line-height: 48px;
            }
            .mdl-card .mdl-card__supporting-text {
                margin-top: 0px;
            }
            .chips {
                float: right;
                vertical-align: middle;
            }

            pre.errorlines {
                background-color: white;
                font-family: monospace;
                border: 1px solid #e0e0e0;
                line-height: 0.9rem;
                font-size: 0.9rem;    padding: 1px 0px 1px; 1px;
                overflow: scroll;
            }
            .prefix {
                color: #660e7a;
                font-weight: bold;
            }
            .attribute {
                color: #0000ff;
                font-weight: bold;
            }
            .value {
                color: #008000;
                font-weight: bold;
            }
            .tag {
                color: #000080;
                font-weight: bold;
            }
            .comment {
                color: #808080;
                font-style: italic;
            }
            .javadoc {
                color: #808080;
                font-style: italic;
            }
            .annotation {
                color: #808000;
            }
            .string {
                color: #008000;
                font-weight: bold;
            }
            .number {
                color: #0000ff;
            }
            .keyword {
                color: #000080;
                font-weight: bold;
            }
            .caretline {
                background-color: #fffae3;
            }
            .lineno {
                color: #999999;
                background-color: #f0f0f0;
            }
            .error {
                display: inline-block;
                position:relative;
                background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4AwCFR4T/3uLMgAAADxJREFUCNdNyLERQEAABMCjL4lQwIzcjErpguAL+C9AvgKJDbeD/PRpLdm35Hm+MU+cB+tCKaJW4L4YBy+CAiLJrFs9mgAAAABJRU5ErkJggg==) bottom repeat-x;
            }
            .warning {
                text-decoration: none;
                background-color: #f6ebbc;
            }
            .overview {
                padding: 10pt;
                width: 100%;
                overflow: auto;
                border-collapse:collapse;
            }
            .overview tr {
                border-bottom: solid 1px #eeeeee;
            }
            .categoryColumn a {
                 text-decoration: none;
                 color: inherit;
            }
            .countColumn {
                text-align: right;
                padding-right: 20px;
                width: 50px;
            }
            .issueColumn {
               padding-left: 16px;
            }
            .categoryColumn {
               position: relative;
               left: -50px;
               padding-top: 20px;
               padding-bottom: 5px;
            }


                </style>
            </head>
            <body class="mdl-color--grey-100 mdl-color-text--grey-700 mdl-base">
            <div class="mdl-layout mdl-js-layout mdl-layout--fixed-header">
                <header class="mdl-layout__header">
                    <div class="mdl-layout__header-row">
                        <span class="mdl-layout-title">Gradle checkDependency result</span>
                        <div class="mdl-layout-spacer"></div>
                        <nav class="mdl-navigation mdl-layout--large-screen-only">
                            Check performed at Fri Mar 04 14:13:14 CST 2022
                        </nav>
                    </div>
                </header>
                <main class="mdl-layout__content">
                    <div class="mdl-layout__tab-panel is-active">
                         <section class="section--center mdl-grid mdl-grid--no-spacing mdl-shadow--2dp"
                     id="OverviewCard" style="display: block;">
                <div class="mdl-card mdl-cell mdl-cell--12-col">
                    <div class="mdl-card__title">
                        <h2 class="mdl-card__title-text">so 大小检查</h2>
                    </div>
                    <div class="mdl-card__supporting-text">
                        <span class="location"><a href="../../build.gradle">../../build.gradle</a>:40</span>:
                        <table class="overview">
                            <tr>
                                <td class="countColumn">1</td>
                                <td class="issueColumn">
                                    <i class="material-icons warning-icon">warning</i>
                                    <a href="#GradleDependency">GradleDependency</a>:Dependency
                                    dasdasdasdasd asdasda
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
            </section>
            <section class="section--center mdl-grid mdl-grid--no-spacing mdl-shadow--2dp"
                     id="GradleDependencyCard" style="display: block;">
                <div class="mdl-card mdl-cell mdl-cell--12-col">
                    <div class="mdl-card__title">
                        <h2 class="mdl-card__title-text">Obsolete Gradle Dependency</h2>
                    </div>
                    <div class="mdl-card__supporting-text">
                        <div class="issue">
                            <div class="warningslist">
                                <span class="location"><a href="../../build.gradle">com.tuya.smart.aar</a></span>
<pre class="errorlines">
<span class="lineno"> 1 </span>    so 文件 <span class="string"> lib/x86/libAMapSDK_MAP_v9_1_0.so </span>文件大小<span class="string"> 10.4MB </span>
<span class="lineno"> 2 </span>    so 文件 <span class="string"> lib/x86/libAMapSDK_MAP_v9_1_0.so </span>文件大小<span class="string"> 10.4MB </span>
</pre>

                                <span class="location"><a href="../../build.gradle">com.tuya.smart.aar</a></span>
                                <pre class="errorlines">
<span class="lineno"> 1 </span>    androidTestImplementation <span class="string">'androidx.test.espresso:espresso-core:3.4.0'</span>
<span class="lineno"> 2 </span>    implementation <span class="string">'androidx.appcompat:appcompat:1.2.0'</span>
<span class="lineno"> 3 </span>    implementation <span class="string">'com.google.android.material:material:1.1.0'</span>
<span class="lineno"> 4 </span>    so <span class="string"> lib/x86/libAMapSDK_MAP_v9_1_0.so fileSize=10.4MB</span>
<span class="lineno"> 5 </span>    so <span class="string"> lib/x86/libAMapSDK_MAP_v9_1_0.so fileSize=10.4MB</span>
<span class="lineno"> 6 </span>    implementation <span class="string">'androidx.constraintlayout:constraintlayout:2.0.4'</span>
</pre>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
                    </div>
                </main>
            </div>
            </body>
            </html>
        """.trimIndent()
    }


//    fun getDivBlock(title: String, desc: String, content: String): String {
////        val title = "so 大小检查"
////        val desc  = "so 文件大小为。"
//        return """
//            <section class="section--center mdl-grid mdl-grid--no-spacing mdl-shadow--2dp"
//                                 id="OverviewCard" style="display: block;">
//                            <div class="mdl-card mdl-cell mdl-cell--12-col">
//                                <div class="mdl-card__title">
//                                    <h2 class="mdl-card__title-text">$title</h2>
//                                </div>
//                                <div class="mdl-card__supporting-text">
//                                    <span class="location">$desc</span>:
//                                    <table class="overview">
//                                    <tr>
//                                        <td class="countColumn">1</td>
//                                        <td class="issueColumn">
//                                            <i class="material-icons warning-icon">warning</i>
//                                            <a href="$path">$pathContent</a>:content
//                                        </td>
//                                    </tr>
//                                    </table>
//                                </div>
//                            </div>
//                        </section>
//        """.trimIndent()
//    }
//
//    fun getCodeBlock(): String {
//        return """
//            <section class="section--center mdl-grid mdl-grid--no-spacing mdl-shadow--2dp"
//                     id="GradleDependencyCard" style="display: block;">
//                <div class="mdl-card mdl-cell mdl-cell--12-col">
//                    <div class="mdl-card__title">
//                        <h2 class="mdl-card__title-text">Obsolete Gradle Dependency</h2>
//                    </div>
//                    <div class="mdl-card__supporting-text">
//                        <div class="issue">
//                            <div class="warningslist">
//                                <span class="location"><a href="文件路径">依赖名称</a></span>
//<pre class="errorlines">
//<span class="lineno"> 1 </span>    so <span class="string">so 文件</span>
//</pre>
//                            </div>
//                        </div>
//                        </div>
//                    </div>
//                </div>
//            </section>
//        """.trimIndent()
//    }
}