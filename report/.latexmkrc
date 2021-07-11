sub makeplantuml { return system "plantuml -teps", $_[0] }
add_cus_dep('puml', 'eps', 0, 'makeplantuml');
$clean_ext .= " eps ist synctex.gz";

sub makeglossaries { return system "makeglossaries", $_[0] }
add_cus_dep('acn', 'acr', 0, 'makeglossaries');
add_cus_dep('glo', 'gls', 0, 'makeglossaries');
$clean_ext .= " acn acr alg glg glo gls glsdefs";
