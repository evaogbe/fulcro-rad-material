# Fulcro RAD Material UI Rendering Plugin

[![Clojars Project](https://img.shields.io/clojars/v/net.clojars.eoogbe/fulcro-rad-material.svg)](https://clojars.org/net.clojars.eoogbe/fulcro-rad-material)

A rendering plugin for [Fulcro RAD](https://github.com/fulcrologic/fulcro-rad) that can render
forms, reports, and containers for a web UI. It leverages [Material UI](https://mui.com/), which is
easy to theme.

Mostly a drop-in replacement for
[Semantic UI rendering plugin v1.2.21](https://github.com/fulcrologic/fulcro-rad-semantic-ui).
All plugin-specific options must be nested under `rendering-options`. Also adds a few things, such
as additional formatters and options. See the config file and the documentation for the options
files for more info.

## Version Compatibility

- Fulcro RAD v1.3.13
- Material UI 5

## Stability Status

This library is alpha quality. It has been tested with the
[fulcro-rad-demo](https://github.com/fulcrologic/fulcro-rad-demo) and various toy projects but has
not yet been used in production. Use at your own risk.

## Usage

Add this library to your Fulcro project and run:

```
yarn add @mui/material @emotion/react @emotion/styled @mui/icons-material
```

Follow
[the instructions here](https://mui.com/material-ui/getting-started/installation/#roboto-font) to
add the Roboto font.

Install the controls on your RAD app:

```clojure
(ns com.example.main
  (:require
    [com.fulcrologic.rad.application :as rad-app]
    [ogbe.fulcro.rad.mui :as mr]))

(defonce app (-> (rad-app/fulcro-rad-app)
                 (rad-app/install-ui-controls! mr/all-controls)))
```

You may also want to add `ogbe.fulcro.mui.utils.css-baseline/ui-css-baseline` to your root
component to [normalize the global CSS](https://mui.com/material-ui/react-css-baseline/).

### Instant type

If you are using the instant type you will also need to
[install the MUI Date Pickers library](https://mui.com/x/react-date-pickers/getting-started/). Then
add the localization provider to your root component:

```clojure
(ns com.example.app.root
  (:require
    [com.fulcrologic.fulcro.components :refer [defsc]]
    ;; For date-fns
    [ogbe.fulcro.mui.date-pickers.adapter-date-fns :refer [adapter-date-fns]]
    ;; or for Day.js
    [ogbe.fulcro.mui.date-pickers.adapter-dayjs :refer [adapter-dayjs]]
    ;; or for Luxon
    [ogbe.fulcro.mui.date-pickers.adapter-luxon :refer [adapter-luxon]]
    ;; or for Moment.js
    [ogbe.fulcro.mui.date-pickers.adapter-moment :refer [adapter-moment]]
    [ogbe.fulcro.mui.date-pickers.localization-provider :refer [ui-localization-provider]]))

(defsc Root [this props]
  (ui-localization-provider
    {:dateAdapter adapter-date-fns}
    ;; Nest other UI elements
  ))
```

#### Warning: joda-time database truncation

The instant controls/form fields use a library dependent on js-joda. Its database is truncated for
compactness. If you need a date before 2016, you will need to require
[`@js-joda/timezone`](https://www.npmjs.com/package/@js-joda/timezone). The library also includes
[reduced file builds](https://github.com/js-joda/js-joda/tree/main/packages/timezone#reducing-js-joda-timezone-file-size)
if you need a subset of the dates.

### Theming

You can surround the top-level UI element of the root component with a theme provider to enable
[custom theming](https://mui.com/material-ui/customization/theming/):

```clojure
(ns com.example.app.root
  (:require
    [com.fulcrologic.fulcro.components :refer [defsc]]
    [ogbe.fulcro.mui.customization.colors :refer [indigo pink]]
    [ogbe.fulcro.mui.customization.styles :refer [create-theme ui-theme-provider]]))

(def theme
  (create-theme {:palette {:primary (indigo)
                           :secondary (pink)}}))

(defsc Root [this props]
  (ui-theme-provider
    {:theme theme}
    ;; Nest other UI elements
  ))
```

## License

Copyright © 2023 Eva Ogbe

Distributed under the MIT License.
