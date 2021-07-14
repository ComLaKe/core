sub makeglossaries { return system "makeglossaries", $_[0] }
add_cus_dep('acn', 'acr', 0, 'makeglossaries');
add_cus_dep('glo', 'gls', 0, 'makeglossaries');
$clean_ext .= " acn acr alg glg glo gls glsdefs ist synctex.gz";
